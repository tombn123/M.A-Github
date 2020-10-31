package System;

import Objects.Blob;
import Objects.Commit;
import Objects.Item;
import common.MagitFileUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MergeConflictsAndMergedItems
{

    //automatic take theirs
    public static final int ONLY_THEIRS_EXIST = 0b010000;                 //=16
    public static final int OURS_IS_THE_SAME_AS_BASE_BUT_THEIRES_IS_DIFFERENT = 0b111101;                 //=61
    //take any
    public static final int ONLY_OURS_AND_THEIRES__WITH_NO_DIFFERENCE = 0b110000;                 //=48
    public static final int ALL_HAS_BUT_NO_DIFFERENCE = 0b111000;                 //=56
    public static final int BASE_IS_DIFFERENT_THAN_OURS_AND_THEIRS = 0b111011;                 //=59
    //automatic take ours
    public static final int ONLY_OURS_EXISTS = 0b100000;                //=32
    public static final int BASE_AND_THEIRS_ARE_THE_SAME_OURS_IS_DIFFERENT = 0b111110;                //=62
    //conflict
    public static final int ONLY_THEIRS_AND_BASE_HAS_WITH_DIFFERENCE = 0b011001;                 //=25
    public static final int ONLY_OURS_AND_BASE_HAS_WITH_DIFFERENCE = 0b101010;                //=42
    public static final int ONLY_OURS_AND_THEIRS_HAS_WITH_DIFFERENCE = 0b110100;                //=52
    public static final int ALL_HAVE_BUT_WITH_DIFFERENCES = 0b111111;                //=63
    Set<Blob> m_mergedItemsNotSorted;
    Set<ConflictingItems> m_conflictItems;
    Boolean m_IsFastForwardCase;
    Commit m_FastForwardCommit;
    //automatic take theirs
    Boolean m_isOurCommitAncestorOfTheirCommit;
    Boolean m_isTheirCommitAncestorOfOurCommit;
    Map<Path, Blob> m_MapOfRelativePathToItemPullingRootFolder;
    //take any
    Map<Path, Blob> m_MapOfRelativePathToItemPulledRootFolder;
    Map<Path, Blob> m_MapOfRelativePathToItemAncestorRootFolder;
    //automatic take ours

    public MergeConflictsAndMergedItems(Set<Blob> i_MergedItemsNotSorted,
                                        Set<ConflictingItems> i_ConflictItems,
                                        Boolean i_IsFastForward,
                                        Commit i_FastForwardCommit,
                                        Boolean i_isPullingAncestorOfPulled,
                                        Boolean i_isPulledAncestorOfPulling,
                                        Map<Path, Blob> i_MapOfRelativePathToItemPullingRootFolder,
                                        Map<Path, Blob> i_MapOfRelativePathToItemPulledRootFolder,
                                        Map<Path, Blob> i_MapOfRelativePathToItemAncestorRootFolder)
    {

        m_mergedItemsNotSorted = i_MergedItemsNotSorted;
        m_conflictItems = i_ConflictItems;
        m_IsFastForwardCase = i_IsFastForward;
        m_FastForwardCommit = i_FastForwardCommit;
        m_isTheirCommitAncestorOfOurCommit = i_isPulledAncestorOfPulling;
        m_isOurCommitAncestorOfTheirCommit = i_isPullingAncestorOfPulled;
        m_MapOfRelativePathToItemPullingRootFolder = i_MapOfRelativePathToItemPullingRootFolder;
        m_MapOfRelativePathToItemPulledRootFolder = i_MapOfRelativePathToItemPulledRootFolder;
        m_MapOfRelativePathToItemAncestorRootFolder = i_MapOfRelativePathToItemAncestorRootFolder;
    }

    public static boolean isConflict(int i_itemState)
    {
        if (i_itemState == ONLY_OURS_AND_THEIRS_HAS_WITH_DIFFERENCE ||
                i_itemState == ALL_HAVE_BUT_WITH_DIFFERENCES ||
                i_itemState == ONLY_OURS_AND_BASE_HAS_WITH_DIFFERENCE ||
                i_itemState == ONLY_THEIRS_AND_BASE_HAS_WITH_DIFFERENCE
        )
            return true;
        else
            return false;
    }

    public static boolean ShouldTakeOurs(int i_itemState)
    {
        if (i_itemState == ONLY_OURS_EXISTS ||
                i_itemState == BASE_AND_THEIRS_ARE_THE_SAME_OURS_IS_DIFFERENT ||
                i_itemState == BASE_IS_DIFFERENT_THAN_OURS_AND_THEIRS ||
                i_itemState == ALL_HAS_BUT_NO_DIFFERENCE ||
                i_itemState == ONLY_OURS_AND_THEIRES__WITH_NO_DIFFERENCE)
            return true;
        else return false;
    }

    public static boolean ShouldTakeTheirs(int i_itemState)
    {
        if (i_itemState == ONLY_THEIRS_EXIST || i_itemState == OURS_IS_THE_SAME_AS_BASE_BUT_THEIRES_IS_DIFFERENT)
            return true;
        else return false;
    }
    //conflict

    public Boolean IsFastForwardCase()
    {
        return m_IsFastForwardCase;
    }

    public Set<Blob> getMergedItemsNotSorted()
    {
        return m_mergedItemsNotSorted;
    }

    public Set<ConflictingItems> GetConflictItems()
    {
        return m_conflictItems;
    }

  /*  public Boolean IsPullingAncestorOfPulled() {
        return m_IsPullingAncestorOfPulled;
    }*/

    public Boolean IsPulledAncestorOfPulling()
    {
        return m_isTheirCommitAncestorOfOurCommit;
    }

    public ObservableList<String> GetConflictItemsNames()
    {
        List<String> conflictNamesList = new ArrayList<>();
        m_conflictItems.forEach(conflictingItem ->
        {
            Blob ourBlob = conflictingItem.m_OurBlob;
            if (ourBlob != null)
                conflictNamesList.add(ourBlob.getName());
            else
            {
                if (conflictingItem.m_TheirBlob != null) ;
                conflictNamesList.add(conflictingItem.m_TheirBlob.getName());
            }
        });
        return FXCollections.observableList(conflictNamesList);
    }

    public Item GetPullingVersionOfConflictDetails(String i_conflictingItem)
    {
        ConflictingItems conflicting = getConflictingItemByName(i_conflictingItem);
        return conflicting.m_OurBlob;
    }

    public ConflictingItems getConflictingItemByName(String i_conflictingItem)
    {
        return m_conflictItems.stream().filter(item -> item.getName().equals(i_conflictingItem)).findFirst().orElse(null);
    }

    public void CreateChosenBlobInWC(String blobText, Blob chosenBlob) throws IOException
    {
        if (chosenBlob == null)
            return;

        MagitFileUtils.WritingStringInFileWholePath(chosenBlob.GetPath().toString(), blobText);
    }
}
