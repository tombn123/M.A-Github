package Objects;

import System.Users.User;
import common.MagitFileUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.Date;

public class Blob extends Item
{
    private String m_Content;


    public Blob(Path i_Path, String i_SHA1, String i_Content, TypeOfFile i_TypeOfFile, User i_UserName,
                Date i_DateOfCreation, String i_BlobName)
    {
        super(i_Path, i_SHA1, i_TypeOfFile, i_UserName, i_DateOfCreation, i_BlobName);
        this.m_Content = i_Content;
    }


    public static String getFileContent(File i_File) throws Exception
    {
        return MagitFileUtils.GetContentFile(i_File);
    }



    public String getContent()
    {
        return m_Content;
    }

}

