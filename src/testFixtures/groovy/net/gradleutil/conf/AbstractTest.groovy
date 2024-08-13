package net.gradleutil.conf

import spock.lang.Specification

import javax.tools.Diagnostic
import javax.tools.DiagnosticCollector
import javax.tools.JavaCompiler
import javax.tools.JavaFileObject
import javax.tools.StandardJavaFileManager
import javax.tools.ToolProvider
import java.nio.file.*

class AbstractTest extends Specification {


    String getPackageName() {
        'net.gradleutil.conf.temp.' + this.class.simpleName.toLowerCase()
    }

    String getBase() {
        'src/test/groovy/' + packageName.replace('.', '/') + '/'
    }

    File getBaseDir() {
        new File(getBase())
    }

    def setup() {
        new File(base).with {
            if (exists()) {
                //deleteDir()
            }
            mkdirs()
        }
    }

    String getResourceText(String resource) {
        getClass().getClassLoader().getResource(resource).text
    }

    static void extractFiles(String path, File outputDirectory) throws IOException {
        URI uri = this.classLoader.getResource(path).toURI()
        if (!uri.path) {
            FileSystems.newFileSystem(uri, Collections.emptyMap(), null).withCloseable { fileSystem ->
                fileSystem.rootDirectories.each {
                    def dirPath = outputDirectory.path
                    Files.walk(it).each {
                        if (it.startsWith('/' + path)) {
                            println 'source ' + it
                            if (it.toString().endsWith('/')) {
                                dirPath += it
                                println 'dir ' + dirPath
                                new File(dirPath).mkdir()
                            } else {
                                println "dirpath ${dirPath}"
                                println "it ${it}"
                                Path targetPath = Paths.get(dirPath.replace(path,'') + it)
                                println "extracting ${it} file://" + targetPath.toAbsolutePath()
                                new File(targetPath.toString()).getParentFile().mkdirs()
                                Files.copy(it, targetPath, StandardCopyOption.REPLACE_EXISTING)
                            }
                        }
                    }
                }
            }
        } else {
            Files.walk(Paths.get(uri)).forEach(source -> {
                String target = path + source.toAbsolutePath().toString().replace(uri.path.replaceAll('/$', ''), '')
                Path destination = Paths.get(outputDirectory.absolutePath, target)
                new File(destination.toString()).getParentFile().mkdir()
                Files.copy(source, destination)
            })
        }
        assert outputDirectory.listFiles().size()
    }

    List<String> getMatchingResourceTexts(String resourcePrefix) {
        URI uri = getClass().getClassLoader().getResource(resourcePrefix).toURI()
        List<String> contents = []
        FileSystems.newFileSystem(uri, Collections.emptyMap(), null).withCloseable { fileSystem ->
            fileSystem.rootDirectories.each {
                Files.walk(it).each {
                    if (it.startsWith('/' + resourcePrefix)) {
                        def text = getClass().getResource(it.toString())?.text
                        if (text) {
                            contents.add(text)
                        }
                    }
                }
            }
        }
        contents
    }


    protected void compileTarget(File targetDir) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler()
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector()
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null)
        Collection allFiles = targetDir.listFiles().findAll { it.name.endsWith('.java') }.collect()
        println '-------------------------------------------------'
        println '--------  COMPILE                ----------------'
        println '-------------------------------------------------'

        List<String> optionList = new ArrayList<String>()
        allFiles.each {
            println("Compiling file://" + it.absolutePath)
        }

        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(allFiles)
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, optionList, null, compilationUnits)

        boolean status = task.call()
        if (!status) {
            for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
                printf("Error on line %s in %s", diagnostic.getLineNumber(), diagnostic)
            }
        }
        fileManager.close()
    }

}
