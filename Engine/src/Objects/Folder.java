package Objects;

import System.FolderDifferences;
import System.Users.User;
import common.CompareItems;
import common.MagitFileUtils;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import static Objects.Item.TypeOfFile.BLOB;
import static Objects.Item.TypeOfFile.FOLDER;
import static System.Repository.WritingStringInAFile;
import static System.Repository.sf_Slash;
import static common.MagitFileUtils.IsMagitFolder;


public class Folder extends Item {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";

    List<Item> m_ListOfItems;
    Map<Path, Item> m_MapOfItems;

    //TODO: ADDING SUITABLE CONSTRUCTOR

    public Folder(List<Item> m_ListOfItems, String i_FolderPath, String i_FolderName,
                  String i_SHA1, TypeOfFile i_TypeOfFile, User i_UserName, Date i_Date) {
        super(Paths.get(i_FolderPath), i_SHA1, i_TypeOfFile, i_UserName, i_Date, i_FolderName);

        this.m_ListOfItems = m_ListOfItems;
        m_MapOfItems = new HashMap<Path, Item>();
        createMapOfItems();
    }

    //TODO: finish this method
    public static FolderDifferences FinedDifferences(Folder i_newRootFolder, Folder i_OldRootFolder) {
        FolderDifferences differences = new FolderDifferences();
        StringBuilder changesBetweenFolders = new StringBuilder();
        Iterator keyIterator = i_newRootFolder.m_MapOfItems.keySet().iterator();
        Map<Path, Item> modifiableNewFolderMap = new HashMap<Path, Item>(i_newRootFolder.m_MapOfItems);
        Map<Path, Item> modifiableOldFolderMap = new HashMap<Path, Item>(i_OldRootFolder.m_MapOfItems);
        while (keyIterator.hasNext()) {
            Path keyPath = (Path) keyIterator.next();
            Item newFolderItem = modifiableNewFolderMap.get(keyPath);
            Item oldFolderItem = modifiableOldFolderMap.get(keyPath);
            if (modifiableOldFolderMap.containsKey(keyPath)) { // exist in the old version of this folder - need to check if its the same or been modified
                //TODO : fix sha1 check
                if (!newFolderItem.getSHA1().equals(oldFolderItem.getSHA1())) {
                    if (newFolderItem.getTypeOfFile() == BLOB)// Item is a blob
                    {
                        //changesBetweenFolders.append(ANSI_YELLOW + "File changed : " + ANSI_RESET + keyPath.toString() + '\n');
                        differences.AddToChangedItemList(newFolderItem);
                    } else//Item is a folder
                    {
                        //changesBetweenFolders.append(ANSI_YELLOW + "Folder changed: " + ANSI_RESET + keyPath.toString() + '\n');
                        differences.AddToChangedItemList(newFolderItem);
                        FolderDifferences changesInsideThisItemFolder = FinedDifferences((Folder) newFolderItem, (Folder) oldFolderItem);
                        differences.AddAnEntireFolderDifference(changesInsideThisItemFolder);
                        //changesBetweenFolders.append(changesInsideThisItemFolder);
                    }
                    modifiableOldFolderMap.remove(keyPath);// remove item so we keep searching for changes
                }
            } else {
                if (newFolderItem.getTypeOfFile() == BLOB) {
                    //changesBetweenFolders.append(ANSI_GREEN + "File added : " + ANSI_RESET + newFolderItem.GetPath().toString() + '\n');

                } else {
                    //changesBetweenFolders.append(ANSI_GREEN + "Folder added : " + ANSI_RESET + newFolderItem.GetPath().toString() + '\n');
                    //String addedItemsInsideThisItemFolder = getAddedFiles((Folder) newFolderItem);
                    //changesBetweenFolders.append(addedItemsInsideThisItemFolder);
                }
                differences.AddToAddedItemList(newFolderItem);
            }
            //was the same so we dont want to mention it anymore
            modifiableNewFolderMap.remove(keyPath);
            modifiableOldFolderMap.remove(keyPath);

        }
        Iterator oldFolderKeysIterator = modifiableOldFolderMap.keySet().iterator();
        while (oldFolderKeysIterator.hasNext()) {
            Path keyPath = (Path) oldFolderKeysIterator.next();
            Item item = (Item) modifiableOldFolderMap.get(keyPath);
            if (item.getTypeOfFile() == BLOB) {
                //changesBetweenFolders.append(ANSI_RED + "File removed : " + ANSI_RESET + keyPath.toString() + System.lineSeparator());

            } else {
                //changesBetweenFolders.append(ANSI_RED + "Folder removed : " + ANSI_RESET + keyPath.toString() + System.lineSeparator());
            }
            differences.AddToRemovedItemList(item);
        }
        return differences;
    }

    private static String getAddedFiles(Folder i_FolderThatHasBeenAdded) {
        StringBuilder addedItems = new StringBuilder();
        Iterator keyIterator = i_FolderThatHasBeenAdded.m_MapOfItems.keySet().iterator();
        while (keyIterator.hasNext()) {
            Item item = i_FolderThatHasBeenAdded.m_MapOfItems.get(keyIterator.next());
            if (item.getTypeOfFile() == BLOB) {
                addedItems.append("File added : " + item.GetPath().toString() + System.lineSeparator());
            } else {
                addedItems.append("Folder added : " + item.GetPath().toString() + System.lineSeparator());
                String insideFolderAddedItems = getAddedFiles((Folder) item);//recursive call for this item which is a folder
                addedItems.append(insideFolderAddedItems);
            }
        }
        return addedItems.toString();
    }

    public static String GetInformation(Folder i_Folder) {
        StringBuilder folderDetailsBuilder = new StringBuilder();
        folderDetailsBuilder.append("Folder:\n");
        folderDetailsBuilder.append("Name: " + i_Folder.GetPath().toString() + "\n");
        folderDetailsBuilder.append("Type: " + i_Folder.getTypeOfFile().toString() + "\n");
        folderDetailsBuilder.append("Sah1: " + i_Folder.getSHA1() + "\n");
        folderDetailsBuilder.append("Changed by : " + i_Folder.getUser().getUserName() + "\n");
        folderDetailsBuilder.append("Time changed : " + Item.getDateStringByFormat(i_Folder.getDate()) + "\n");
        List<Item> folderListOfItems = i_Folder.m_ListOfItems;
        for (int i = 0; i < folderListOfItems.size(); i++) {
            if (folderListOfItems.get(i).getTypeOfFile().equals(FOLDER)) {
                folderDetailsBuilder.append(GetInformation((Folder) folderListOfItems.get(i)));
            } else {
                StringBuilder blobDetailsBuilder = new StringBuilder();
                blobDetailsBuilder.append("Blob:\n");
                blobDetailsBuilder.append("Name: " + folderListOfItems.get(i).GetPath().toString() + "\n");
                blobDetailsBuilder.append("Type: " + folderListOfItems.get(i).getTypeOfFile().toString() + "\n");
                blobDetailsBuilder.append("Sah1: " + folderListOfItems.get(i).getSHA1() + "\n");
                blobDetailsBuilder.append("Changed by : " + folderListOfItems.get(i).getUser().getUserName() + "\n");
                blobDetailsBuilder.append("Time changed : " + Item.getDateStringByFormat(folderListOfItems.get(i).getDate()) + "\n");
                folderDetailsBuilder.append(blobDetailsBuilder.toString());
            }

        }
        return folderDetailsBuilder.toString();
    }

    public static void SpanDirectory(Folder i_RootFolder) throws IOException {
        Path folderPath = i_RootFolder.GetPath();
        folderPath.toFile().mkdir();
        Iterator itemsIterator = i_RootFolder.m_ListOfItems.iterator();
        while (itemsIterator.hasNext()) {
            Item folderItem = (Item) itemsIterator.next();
            if (folderItem.getTypeOfFile().equals(FOLDER)) {
                SpanDirectory((Folder) folderItem);
            } else {
                Blob currentBlob = (Blob) folderItem;
                MagitFileUtils.WritingFileByPath(currentBlob.GetPath().toString(), currentBlob.getContent());
            }
        }
    }

    public static void RemoveFilesAndFoldersWithoutMagit(Path i_FolderPathToRemove) throws IOException {
        File[] rootFolder = new File(i_FolderPathToRemove.toString()).listFiles();

        for (File currentFile : rootFolder) {
            if (!currentFile.getName().equals(".magit")) {
                if (currentFile.isDirectory() == true) {
                    org.apache.commons.io.FileUtils.deleteDirectory(currentFile);
                } else {
                    currentFile.delete();
                }
            }
        }
    }

    public static Folder CreateFolderFromTextFolder(File i_TextFolder, Path i_PathToThisFolder, String i_foldersSha1, User i_user, Date i_Date, Path i_ObjectsFolderPath) throws Exception {   //example: 123,50087888a7c34344416ec0fd600f394dadf3d9d8,FOLDER,Administrator,06.39.2019-06:39:27:027
        Path tempFolderPath = Paths.get(i_ObjectsFolderPath.getParent().toString() + "\\Temp");
        if (!tempFolderPath.toFile().exists()) {
            tempFolderPath.toFile().mkdir();
        }
        List<Item> folderListOfItems = new ArrayList<Item>();
        String FoldersName = i_PathToThisFolder.getFileName().toString();
        Path foldersPath = i_PathToThisFolder;
        String foldersSha1 = i_foldersSha1;
        TypeOfFile type = FOLDER;
        User user = i_user;
        Date date = i_Date;
        try {
            Scanner lineScanner = new Scanner(i_TextFolder);
            while (lineScanner.hasNext()) {
                String lineOfDetails = lineScanner.nextLine();
                if (Item.IsAFile(lineOfDetails)) {
                    // a line that represent a blob, hence we parse it accordingly
                    Blob thisBlob;
                    String[] itemsDetails = Item.GetItemsDetails(lineOfDetails);
                    Path filePath = Paths.get(i_PathToThisFolder.toString() + "\\" + itemsDetails[0]);
                    String fileSha1 = itemsDetails[1];
                    User fileUser = new User(itemsDetails[3]);
                    Date fileDate = new Date();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-hh:mm:ss:SSS");
                    fileDate = Item.ParseDateWithFormat(itemsDetails[4]);

                    //Blob(File i_File, String i_Sha1, String i_FileContent, TypeOfFile i_TypeOfFile, User i_CurrentUser, String i_BlobName)
                    Path zippedBlobPathInObjectsFolder = Paths.get(i_ObjectsFolderPath.toString() + "\\" + fileSha1);
                    Path tempBlobFile = Item.UnzipFile(zippedBlobPathInObjectsFolder, tempFolderPath);

                    String blobContent = Blob.getFileContent(tempBlobFile.toFile());
                    //Blob(File i_File, String i_Sha1, String i_FileContent, TypeOfFile i_TypeOfFile, User i_CurrentUser, String i_BlobName) {
                    thisBlob = new Blob(filePath, fileSha1, blobContent, BLOB, fileUser, fileDate, filePath.getFileName().toString());
                    folderListOfItems.add(thisBlob);


                } else {//if it a folder
                    //123, 50087888a7c34344416ec0fd600f394dadf3d9d8, FOLDER, Administrator, 06.39.2019-06:39:27:027
                    String[] folderDetails = Item.GetItemsDetails(lineOfDetails);
                    String nameOfFolder = folderDetails[0];
                    Path folderZippedInObjectsFolderPath = Paths.get(i_ObjectsFolderPath.toString() + "\\" + folderDetails[1]);
                    Path tempTextFolderPathUnzipped = Item.UnzipFile(folderZippedInObjectsFolderPath, tempFolderPath);
                    User folderUser = new User(folderDetails[3]);
                    Date fileDate = new Date();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-hh:mm:ss:SSS");
                    fileDate = Item.ParseDateWithFormat(folderDetails[4]);
                    Path FolderPathInRootFolder = Paths.get(i_PathToThisFolder.toString() + "\\" + nameOfFolder);
                    Folder currentItemFolder = CreateFolderFromTextFolder(tempTextFolderPathUnzipped.toFile(), FolderPathInRootFolder, folderDetails[1], folderUser, fileDate, i_ObjectsFolderPath);
                    folderListOfItems.add(currentItemFolder);
                }

            }
        } catch (Exception e) {
            throw e;
        }

        return new Folder(folderListOfItems,
                i_PathToThisFolder.toString(),
                FoldersName,
                i_foldersSha1,
                FOLDER,
                user,
                date);

    }

    public static Folder createInstanceOfFolder(Path i_FolderPath, User i_CurrentUser, Map<Path, Item> i_ItemsMapWithPaths) throws Exception {
        List<Item> allItemsInCurrentFolder = new ArrayList<>();

        File[] files = new File(i_FolderPath.toString()).listFiles();
        for (File file : files) {
            if (!IsMagitFolder(file)) {
                if (file.isDirectory()) {
                    Folder currentFolderReturnedFromRecursion = createInstanceOfFolder(file.toPath(), i_CurrentUser, i_ItemsMapWithPaths);
                    allItemsInCurrentFolder.add(currentFolderReturnedFromRecursion);
                } else {
                    String sha1 = createSHA1ForTextFile(file);
                    Blob currentBlob = new Blob(file.toPath(), sha1, MagitFileUtils.GetContentFile(file),
                            TypeOfFile.BLOB, i_CurrentUser, new Date(), file.getName());
                    allItemsInCurrentFolder.add(currentBlob);
                }
            }
        }

        for (int i = 0; i < allItemsInCurrentFolder.size(); i++) {
            i_ItemsMapWithPaths.put(allItemsInCurrentFolder.get(i).GetPath(), allItemsInCurrentFolder.get(i));
        }
        Folder currentFolder = new Folder(allItemsInCurrentFolder, i_FolderPath.toString(), i_FolderPath.getFileName().toString(),
                CreateSHA1ForFolderFile(allItemsInCurrentFolder), TypeOfFile.FOLDER, i_CurrentUser, new Date());

        return currentFolder;

    }

    public static String CreateSHA1ForFolderFile(List<Item> i_AllItems) {
        StringBuilder stringForCreatingSHA1 = new StringBuilder();
        Collections.sort(i_AllItems, new CompareItems());
        for (Item currentItemInList : i_AllItems) {
            stringForCreatingSHA1.append(currentItemInList.getName());
            stringForCreatingSHA1.append(currentItemInList.getSHA1());
            stringForCreatingSHA1.append(currentItemInList.getTypeOfFile().toString());
        }

        return DigestUtils.sha1Hex(stringForCreatingSHA1.toString());
    }

    private static String createSHA1ForTextFile(File i_File) throws Exception {
        String stringForSha1 = MagitFileUtils.GetContentFile(i_File);

        return DigestUtils.sha1Hex(stringForSha1);
    }

    public static boolean isDirEmpty(final Path directory) throws IOException {
        //TODO: currently sees .magit in folder so it is not empty all the time - instead we need to ignor .magit
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        }
    }

    public static void DeleteDirectory(String i_LocationOfFolderToDelete) throws IOException {
        Path pathOfFolderToDelete = Paths.get(i_LocationOfFolderToDelete);
        org.apache.commons.io.FileUtils.deleteDirectory(pathOfFolderToDelete.toFile());
    }

    public static boolean IsFileExist(String i_LocationOfFile) {
        File tempFileForCheckExistence = new File(i_LocationOfFile);

        return tempFileForCheckExistence.exists();
    }


    //should replace createTempCommitWithoutCreatingObjects() if possible

    private void createMapOfItems() {
        Iterator itemsIterator = m_ListOfItems.iterator();
        while (itemsIterator.hasNext()) {
            Item item = (Item) itemsIterator.next();
            m_MapOfItems.put(item.GetPath(), item);
        }
    }

    public Integer GetAmountOfItems() {
        return m_ListOfItems.size();
    }

    public List<Item> getListOfItems() {
        return m_ListOfItems;
    }

    public Path WritingFolderAsATextFile() {
        Path pathFileForWritingString;
        //TODO: CHECK IF POSSIBLE SWITCH TO STREAM METHOD

        String fileContent = convertFolderToString(this, this.getUser());

        pathFileForWritingString = WritingStringInAFile(fileContent, this.getSHA1());

        return pathFileForWritingString;
    }

    private String convertFolderToString(Folder i_Folder, User i_CurrentUser) {
        StringBuilder resultString = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-hh:mm:ss:SSS");

        for (Item item : i_Folder.getListOfItems()) {
            // TODO: ORDER BY REQUIREMENT
            resultString.append(item.getName() + ",");
            resultString.append(item.getSHA1() + ",");
            resultString.append(item.getTypeOfFile().toString() + ",");
            resultString.append(i_CurrentUser.getUserName() + ",");
            resultString.append(dateFormat.format(item.getDate()) + "\n");
        }

        return resultString.toString();
    }

    public void initFolderPaths(Path i_NewPathOfRepository) {
        this.m_Path = i_NewPathOfRepository;

        for (Item item : this.m_ListOfItems) {
            if (item.getTypeOfFile().equals(TypeOfFile.FOLDER)) {
                Folder currentFolder = (Folder) item;
                currentFolder.initFolderPaths(Paths.get(i_NewPathOfRepository + sf_Slash + currentFolder.getName()));
            } else
                item.setPath(Paths.get(i_NewPathOfRepository + sf_Slash + item.getName()));
        }
    }

    public Set<Blob> GetSetOfBlobs() {
        return getSetOfBlobsFromFolder(this);
    }

    private Set<Blob> getSetOfBlobsFromFolder(Folder i_Folder) {
        Set<Blob> setOfBlobs = new HashSet<Blob>();
        i_Folder.m_ListOfItems.forEach(item -> {
            if (item.getTypeOfFile().equals(FOLDER)) {
                Set<Blob> innerSetOfBlobs = getSetOfBlobsFromFolder((Folder) item);
                innerSetOfBlobs.forEach(blob -> {
                    if (!setOfBlobs.contains(blob))
                        setOfBlobs.add(blob);
                });
            } else {
                setOfBlobs.add((Blob) item);
            }

        });
        return setOfBlobs;
    }

    public List<Item> GetFirstLayerOfItems() {
        return m_ListOfItems;
    }

    public Map<Path, Item> GetMapOfItems() {
        return m_MapOfItems;
    }

    public Map<Path, Item> getAllItemsMap() {
        Map<Path, Item> resMap = new HashMap<Path, Item>();
        resMap.put(this.m_Path,this);
        Iterator<Item> itemsIterator = this.getListOfItems().iterator();
        while (itemsIterator.hasNext()) {
            Item currItem = itemsIterator.next();
            resMap.put(currItem.GetPath(), currItem);
            if (currItem.getTypeOfFile().equals(FOLDER)) {
                Map<Path, Item> folderMap = ((Folder) currItem).getAllItemsMap();
                addMapItems(resMap, folderMap);
            }
        }
        return resMap;
    }

    private void addMapItems(Map<Path,Item> i_MapToAddTo, Map<Path,Item> i_MapToAdd) {
        Set<Path> mapToAddKeySet = i_MapToAdd.keySet();
        Iterator<Path> keySetIterator = mapToAddKeySet.iterator();
        while (keySetIterator.hasNext()){
            Path currKey = keySetIterator.next();
            i_MapToAddTo.put(currKey,i_MapToAdd.get(currKey));
        }
    }

    /*public void removeItem(Item fileToDelete) {
        this.getListOfItems().forEach(item -> {
        });
    }*/
}
