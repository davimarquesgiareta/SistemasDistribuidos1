package java8watchserviceexample;
 
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import static java.nio.file.StandardWatchEventKinds.*;
 
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
 
public class Java8WatchServiceExample {
 
    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
 
    /**
     * Creates a WatchService and registers the given directory
     */
    Java8WatchServiceExample(Path dir) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey, Path>();
 
        walkAndRegisterDirectories(dir);
    }
 
    /**
     * Register the given directory with the WatchService; This function will be called by FileVisitor
     */
    private void registerDirectory(Path dir) throws IOException 
    {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keys.put(key, dir);
    }
 
    /**
     * Register the given directory, and all its sub-directories, with the WatchService.
     */
    private void walkAndRegisterDirectories(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                registerDirectory(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
 
    /**
     * Process all events for keys queued to the watcher
     */
    void processEvents() {
        for (;;) {
 
            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }
 
            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }
 
            for (WatchEvent<?> event : key.pollEvents()) {
                @SuppressWarnings("rawtypes")
                WatchEvent.Kind kind = event.kind();
 
                // Context for directory entry event is the file name of entry
                @SuppressWarnings("unchecked")
                Path name = ((WatchEvent<Path>)event).context();
                Path child = dir.resolve(name);
 
                // print out event
                //event.kind().name() é a ação - CRIAR, DELETE, ATUALIZAR
                //child é o diretório/arquivo 
                System.out.format("%s %s\n", event.kind().name(), child);
                
                // if directory is created, and watching recursively, then register it and its sub-directories
                if (kind == ENTRY_CREATE || kind == ENTRY_MODIFY) {
                    try {
                        copiarArquivo(child);
                        if (Files.isDirectory(child)) {
                            walkAndRegisterDirectories(child);
                            
                        }
                    } catch (IOException x) {
                        // do something useful
                    }
                }
                
                if (kind == ENTRY_DELETE){
                    deletarArquivo(child);
                }
                
                
            }
                
 
            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);
                
                // all directories are inaccessible
                if (keys.isEmpty()) {
                  
                    break;
                }
            }
        }
    }
    
    public static void deletarArquivo (Path child){
        System.out.println("ENTROU NA DELETANCIA");
        System.out.println(" E O CHILD? : "+child);
         String[] caminhosplit = new String[6];
         String caminho = child.toString();
         caminhosplit = caminho.split("pastatestes");
                
             System.out.println("Caminho Split");
             for (int i = 0; i < caminhosplit.length; i++) {
                 System.out.println("->  "+caminhosplit[i]);
        }
             
            String baseCaminhoBackup1 = "C:\\Users\\davim\\Desktop\\backup1";
            String baseCaminhoBackup2 = "C:\\Users\\davim\\Desktop\\backup2";
            String caminhoCompleto1 = baseCaminhoBackup1.concat(caminhosplit[1]);
            String caminhoCompleto2 = baseCaminhoBackup2.concat(caminhosplit[1]);
            
            System.out.println("1 - " + caminhoCompleto1);
             System.out.println("2 - " + caminhoCompleto2);
        
        File f1 = new File(caminhoCompleto1);  
        File f2 = new File(caminhoCompleto2);  
        f1.delete();
        f2.delete();
    }
 
    public static void copiarArquivo(Path child) {
              String[] caminhosplit = new String[6];
              String caminho = child.toString();
             caminhosplit = caminho.split("pastatestes");
                
             System.out.println("Caminho Split");
             for (int i = 0; i < caminhosplit.length; i++) {
                 System.out.println("->  "+caminhosplit[i]);
        }
             
             
          
        try {
            System.out.println("o famoso child é "+ child);
            String inFileName = caminho;
            String baseCaminhoBackup1 = "C:\\Users\\davim\\Desktop\\backup1";
            String baseCaminhoBackup2 = "C:\\Users\\davim\\Desktop\\backup2";
            String caminhoCompleto1 = baseCaminhoBackup1.concat(caminhosplit[1]);
            String caminhoCompleto2 = baseCaminhoBackup2.concat(caminhosplit[1]);
            
            System.out.println("Caminho 1" + caminhoCompleto1);
            System.out.println("Caminho2 " +caminhoCompleto2);
            
            String outFileName = caminhoCompleto1;
            String outFileName2 = caminhoCompleto2;

            FileInputStream in = new FileInputStream(inFileName);
            FileOutputStream out = new FileOutputStream(outFileName);
            FileOutputStream out2 = new FileOutputStream(outFileName2);
            
            
            byte[] buf = new byte[1024];
            int len;

            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
                out2.write(buf, 0, len);
            }

            out.close();
            out2.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    public static void main(String[] args) throws IOException {
        Path dir = Paths.get("C:\\Users\\davim\\Desktop\\pastatestes");
        new Java8WatchServiceExample(dir).processEvents();
    }

   
}