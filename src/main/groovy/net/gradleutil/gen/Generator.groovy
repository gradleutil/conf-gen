package net.gradleutil.gen

import gg.jte.ContentType
import gg.jte.TemplateEngine
import gg.jte.compiler.TemplateCompiler
import gg.jte.html.HtmlPolicy
import gg.jte.resolve.DirectoryCodeResolver
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import groovy.util.logging.Log
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
            def loader = new GroovyClassLoader(Generator.class.classLoader)
            loader.addURL(path.toUri().toURL())
            classLoaders.put(path, loader)
            return loader
        }
        logger.info("using existing classloader for ${path}")
        classLoader
    }

    static TemplateEngine getTemplateEngine(GeneratorOptions options = defaultOptions()) {
        logger.info('loading templates with default options: ' + options.dump() )
        TemplateEngine.createPrecompiled(getJarPath(Generator), ContentType.Plain, options.classLoader, options.packageName).
                tap { setTrimControlStructures(true) }
    }

    static TemplateEngine getTemplateEngine(Path zipPath) {
        logger.info('loading templates from ' + zipPath)
        TemplateEngine.createPrecompiled(zipPath, ContentType.Plain, getLoader(zipPath), defaultOptions().packageName).
                tap { setTrimControlStructures(true) }
    }


    @Builder(builderStrategy = SimpleStrategy, prefix = '')
    static class GeneratorOptions {
        ContentType contentType
        Boolean trimControlStructures
        String[] htmlTags
        String[] htmlAttributes
        Boolean htmlCommentsPreserved
        Boolean binaryStaticContent
        File tempDirectory

        List<File> compilePath
        ClassLoader classLoader
        String packageName
        String htmlPolicyClass
        String[] compileArgs
    }

    static GeneratorOptions defaultOptions() {
        new GeneratorOptions().contentType(ContentType.Plain)
                .classLoader(Generator.classLoader)
                .packageName('net.gradleutil.conf.jte')
                .tempDirectory(File.createTempDir('gen', 'files'))
    }

    static ClassLoader jar(Path sourceDirectory, Path zipPath, GeneratorOptions options = defaultOptions()) {
        def classLoader = compile(sourceDirectory, options.tempDirectory.toPath(), options)
        def zipped = 0
        new ZipOutputStream(new FileOutputStream(zipPath.toFile())).withCloseable { zipFile ->
            new File(options.tempDirectory.path).eachFileRecurse { file ->
                if (file.isFile()) {
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
        classLoaders.put(zipPath, classLoader)
        classLoader
    }

    static ClassLoader compile(Path sourceDirectory, Path targetDirectory, GeneratorOptions options = defaultOptions()) {
        long start = System.nanoTime()
        targetDirectory.toFile().deleteDir()
        if (!targetDirectory.toFile().mkdirs()) {
            throw new RuntimeException("could not create target directory")
        }

        logger.info("Pre-compiling jte templates found in " + sourceDirectory)

        def classLoader = new GroovyClassLoader(options.classLoader, new CompilerConfiguration(targetDirectory: targetDirectory.toFile()))
        options.compilePath.each {
            classLoader.addClasspath(it.absolutePath)
        }
        parseFolder(sourceDirectory, classLoader)

        TemplateEngine templateEngine = TemplateEngine.create(new DirectoryCodeResolver(sourceDirectory), targetDirectory, options.contentType, classLoader, options.packageName)

        templateEngine.setTrimControlStructures(Boolean.TRUE == options.trimControlStructures)
        templateEngine.setHtmlTags(options.htmlTags)
        templateEngine.setHtmlAttributes(options.htmlAttributes)
        if (options.htmlPolicyClass != null) {
            Class<?> htmlPolicyClass = classLoader.loadClass(options.htmlPolicyClass)
            templateEngine.setHtmlPolicy((HtmlPolicy) htmlPolicyClass.getConstructor().newInstance())
        }
        templateEngine.setHtmlCommentsPreserved(Boolean.TRUE == options.htmlCommentsPreserved)
        templateEngine.setBinaryStaticContent(Boolean.TRUE == options.binaryStaticContent)
        templateEngine.setCompileArgs(options.compileArgs)

        int numberCompiled
        try {
            templateEngine.cleanAll()
            List<String> compilePathFiles = getCompileFilePath(options, classLoader, targetDirectory.toFile())
            numberCompiled = templateEngine.precompileAll(compilePathFiles).size()
        } catch (Exception e) {
            logger.severe("Failed to precompile templates.")
            throw e
        }

        long end = System.nanoTime()
        long duration = TimeUnit.NANOSECONDS.toSeconds(end - start)
        logger.info("Successfully precompiled " + numberCompiled + " jte file" + (numberCompiled == 1 ? "" : "s") + " in " + duration + "s to " + targetDirectory)
        classLoader
    }

    static List<String> getCompileFilePath(GeneratorOptions options, ClassLoader classLoader, File targetDirectory) {
        List<String> compilePathFiles = options.compilePath.collect { it.absolutePath }
        def loader
        loader = classLoader
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
                println file
                if (file.toFile().path.endsWith('.groovy') || file.toFile().path.endsWith('.java')) {
                    println(file.toFile())
                    classLoader.parseClass(file.toFile())
                }
                return FileVisitResult.CONTINUE
            }
        })
    }

}
