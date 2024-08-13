package net.gradleutil.gen

import gg.jte.ContentType
import gg.jte.TemplateEngine
import gg.jte.html.HtmlPolicy
import gg.jte.resolve.DirectoryCodeResolver
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import groovy.util.logging.Log
import net.gradleutil.conf.template.EPackage
import net.gradleutil.conf.util.ChildFirstClassLoader
import org.codehaus.groovy.control.CompilerConfiguration

import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Log(value = "logger")
class Generator {

    private static Map<Path, ClassLoader> classLoaders = [:]

    static Path getJarPath(Class clazz) {
        Paths.get(clazz.getProtectionDomain().getCodeSource().getLocation().toURI())
    }

    static ClassLoader getLoader(Path path) {
        ClassLoader classLoader = classLoaders.find { it.key == path }?.value
        if (!classLoader) {
            logger.info("Creating classloader for ${path}")
            def loader = new URLClassLoader(path.toUri().toURL() as URL[], Generator.class.classLoader)
            classLoaders.put(path, loader)
            return loader
        }
        logger.info("using existing classloader for ${path}")
        classLoader
    }

    static TemplateEngine getTemplateEngine(GeneratorOptions options = generatorOptions()) {
        logger.info('loading templates with default options: ' + options.dump() )
        TemplateEngine.createPrecompiled(getJarPath(Generator), ContentType.Plain, options.classLoader, options.packageName).
                tap { setTrimControlStructures(true) }
    }

    static TemplateEngine getTemplateEngine(Path zipPath) {
        logger.info('loading templates from ' + zipPath)
        TemplateEngine.createPrecompiled(zipPath, ContentType.Plain, getLoader(zipPath), generatorOptions().packageName).
                tap { setTrimControlStructures(true) }
    }


    static File getJar(EPackage ePackage, File sourceDir) {
        logger.info('loading ePackage ' + ePackage.name + ' from ' + sourceDir.absoluteFile.toString())
        def target = new File(sourceDir, 'jte.jar').absoluteFile
        if (!sourceDir.exists()) {
            throw new RuntimeException("Source dir $sourceDir.absolutePath does not exist")
        }
        if (!target.exists()) {
            jar(sourceDir.toPath(), target.toPath())
        }
        target
    }


    @Builder(builderStrategy = SimpleStrategy, prefix = '')
    static class GeneratorOptions {
        ContentType contentType
        Boolean trimControlStructures
        String[] htmlTags
        Boolean htmlCommentsPreserved
        Boolean binaryStaticContent
        File tempDirectory

        List<File> compilePath
        ClassLoader classLoader
        String packageName
        String htmlPolicyClass
        String[] compileArgs
    }

    static GeneratorOptions generatorOptions() {
        new GeneratorOptions().contentType(ContentType.Plain)
                .classLoader(Generator.classLoader)
                .packageName('net.gradleutil.conf.jte')
                .tempDirectory(File.createTempDir('gen', 'files'))
    }

    static ClassLoader jar(Path sourceDirectory, Path zipPath, GeneratorOptions options = generatorOptions()) {
        def classLoader = compile(sourceDirectory, options.tempDirectory.toPath(), options)
        def zipped = 0
        new ZipOutputStream(new FileOutputStream(zipPath.toFile())).withCloseable { zipFile ->
            new File(options.tempDirectory.path).eachFileRecurse { file ->
                if (file.isFile() && !file.name.endsWith('.java')) {
                    def path = file.absolutePath.replace(options.tempDirectory.absolutePath + '/', '')
                    zipFile.putNextEntry(new ZipEntry(path))
                    def buffer = new byte[file.size()]
                    file.withInputStream { zipFile.write(buffer, 0, it.read(buffer)) }
                    zipFile.closeEntry()
                    zipped++
                }
            }
        }
        logger.info("zipped ${zipped} files")
        ChildFirstClassLoader childFirstClassLoader = new ChildFirstClassLoader([zipPath.toUri().toURL()] as URL[], classLoader)
        logger.info("Using child loader for ${zipPath.toAbsolutePath()}")
        classLoaders.put(zipPath, childFirstClassLoader)
        classLoader
    }

    static synchronized ClassLoader compile(Path sourceDirectory, Path targetDirectory, GeneratorOptions options = generatorOptions()) {
        long start = System.nanoTime()
        logger.info("Compiling jte templates found in " + sourceDirectory)
        TemplateEngine templateEngine = getTemplateEngine(sourceDirectory, targetDirectory, options)
        int numberCompiled
        try {
            templateEngine.cleanAll()
            List<String> compilePathFiles = getCompileFilePath(options, targetDirectory.toFile())
            numberCompiled = templateEngine.precompileAll(compilePathFiles).size()
        } catch (Exception e) {
            logger.severe("Failed to compile templates.")
            throw e
        }

        long end = System.nanoTime()
        long duration = TimeUnit.NANOSECONDS.toSeconds(end - start)
        logger.info("Successfully compiled " + numberCompiled + " jte file" + (numberCompiled == 1 ? "" : "s") + " in " + duration + "s to " + targetDirectory)
        options.classLoader
    }

    static ClassLoader generate(Path sourceDirectory, Path targetDirectory, GeneratorOptions options = generatorOptions()) {
        logger.info("generating jte templates found in " + sourceDirectory)
        TemplateEngine templateEngine = getTemplateEngine(sourceDirectory, targetDirectory, options)
        templateEngine.cleanAll()
        int numberGenerated = templateEngine.generateAll().size()
        logger.info("Successfully generated " + numberGenerated + " jte file" + (numberGenerated == 1 ? "" : "s") + " to " + targetDirectory)
        options.classLoader
    }

    static TemplateEngine getTemplateEngine(Path sourceDirectory, Path targetDirectory, GeneratorOptions options = generatorOptions()) {
        def classLoader = new GroovyClassLoader(options.classLoader, new CompilerConfiguration(targetDirectory: targetDirectory.toFile()))
        options.compilePath.each {
            classLoader.addClasspath(it.absolutePath)
        }
        parseFolder(sourceDirectory, classLoader)

        TemplateEngine templateEngine = TemplateEngine.create(new DirectoryCodeResolver(sourceDirectory), targetDirectory, options.contentType, classLoader, options.packageName)

        templateEngine.setTrimControlStructures(Boolean.TRUE == options.trimControlStructures)
        templateEngine.setHtmlTags(options.htmlTags)
        if (options.htmlPolicyClass != null) {
            Class<?> htmlPolicyClass = classLoader.loadClass(options.htmlPolicyClass)
            templateEngine.setHtmlPolicy((HtmlPolicy) htmlPolicyClass.getConstructor().newInstance())
        }
        templateEngine.setHtmlCommentsPreserved(Boolean.TRUE == options.htmlCommentsPreserved)
        templateEngine.setBinaryStaticContent(Boolean.TRUE == options.binaryStaticContent)
        templateEngine.setCompileArgs(options.compileArgs)
        options.classLoader = classLoader
        templateEngine
    }

    static List<String> getCompileFilePath(GeneratorOptions options, File targetDirectory) {
        List<String> compilePathFiles = options.compilePath.collect { it.absolutePath }
        def loader
        loader = options.classLoader
        while (loader) {
            if (loader instanceof URLClassLoader) {
                loader.getURLs().each { compilePathFiles.add(it.file) }
            }
            loader = loader.getParent()
        }
        System.getProperty("java.class.path").split(File.pathSeparator).each {
            compilePathFiles.add(it)
        }
        compilePathFiles.add(targetDirectory.absolutePath)
        compilePathFiles
    }

    static void parseFolder(Path source, GroovyClassLoader classLoader) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

            @Override
            FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                classLoader.addClasspath(dir.toFile().absolutePath)
                return FileVisitResult.CONTINUE
            }

            @Override
            FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.toFile().path.endsWith('.groovy') || file.toFile().path.endsWith('.java')) {
                    println('adding file to classloader for some reason ' + file.toFile())
                    classLoader.parseClass(file.toFile())
                }
                return FileVisitResult.CONTINUE
            }
        })
    }

}
