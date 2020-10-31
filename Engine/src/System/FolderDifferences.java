package System;

import Objects.Blob;
import Objects.Item;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class FolderDifferences {
    private List<Item> m_AddedItemList;
    private List<Item> m_RemovedItemList;
    private List<Item> m_ChangedItemList;

    public FolderDifferences() {
        m_AddedItemList = new ArrayList<>();
        m_RemovedItemList = new ArrayList<>();
        m_ChangedItemList = new ArrayList<>();
    }

    public List<Item> getAddedItemList() {
        return m_AddedItemList;
    }

    public List<Item> getRemovedItemList() {
        return m_RemovedItemList;
    }

    public List<Item> getChangedItemList() {
        return m_ChangedItemList;
    }

    public void AddToAddedItemList(Item i_AddedItem) {
        m_AddedItemList.add(i_AddedItem);
    }

    public void AddToRemovedItemList(Item i_RemovedItem) {
        m_RemovedItemList.add(i_RemovedItem);
    }

    public void AddToChangedItemList(Item i_ChangedItem) {
        m_ChangedItemList.add(i_ChangedItem);
    }

    public void AddAnEntireFolderDifference(FolderDifferences i_FolderDifference) {
        for (int i = 0; i < i_FolderDifference.m_AddedItemList.size(); i++) {
            m_AddedItemList.add(i_FolderDifference.m_AddedItemList.get(i));
        }
        for (int i = 0; i < i_FolderDifference.m_ChangedItemList.size(); i++) {
            m_ChangedItemList.add(i_FolderDifference.m_ChangedItemList.get(i));
        }
        for (int i = 0; i < i_FolderDifference.m_RemovedItemList.size(); i++) {
            m_RemovedItemList.add(i_FolderDifference.m_RemovedItemList.get(i));
        }
    }

    public void SumInFolderDiffernce(FolderDifferences i_FolderDifference) {
        i_FolderDifference.m_AddedItemList.forEach(item -> {
            this.m_AddedItemList.add(item);
        });
        i_FolderDifference.m_ChangedItemList.forEach(item -> {
            this.m_ChangedItemList.add(item);
        });
        i_FolderDifference.m_RemovedItemList.forEach(item -> {
            this.m_RemovedItemList.add(item);
        });

    }

    public boolean isDeletedFile(String i_FileName) {
        AtomicBoolean res = new AtomicBoolean(false);
        this.m_RemovedItemList.forEach(item -> {
            if(item.getName().equals(i_FileName))
                res.set(true);
        });
        return res.get();
    }

    public boolean isAddedFile(String i_FileName) {
        AtomicBoolean res = new AtomicBoolean(false);
        this.m_AddedItemList.forEach(item -> {
            if(item.getName().equals(i_FileName))
                res.set(true);
        });
        return res.get();
    }

    public boolean isChangedFile(String i_FileName) {
        AtomicBoolean res = new AtomicBoolean(false);
        this.m_ChangedItemList.forEach(item -> {
            if(item.getName().equals(i_FileName))
                res.set(true);
        });
        return res.get();
    }

    public Item getItemFromAddedList(String i_FileName) {
        Item item = null;
        Iterator<Item> itemIterator = m_AddedItemList.iterator();
        while(itemIterator.hasNext()){
            item = itemIterator.next();
            if(item.getName().equals(i_FileName)){
                return item;
            }
        }
        return item;
    }

    public Item getItemFromChangedList(String i_FileName) {
        Item item = null;
        Iterator<Item> itemIterator = m_ChangedItemList.iterator();
        while(itemIterator.hasNext()){
            item = itemIterator.next();
            if(item.getName().equals(i_FileName)){
                return item;
            }
        }
        return item;
    }
}

