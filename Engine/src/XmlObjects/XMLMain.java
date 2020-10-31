package XmlObjects;

import Objects.Folder;
import System.Engine;
import System.Repository;
import XmlObjects.repositoryWriters.LocalRepositoryWriter;
import XmlObjects.repositoryWriters.RepositoryWriter;
import collaboration.LocalRepository;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;

import static common.MagitFileUtils.IsMagitFolder;

// this class is a builder for a repository from an xml:
// 1) checks that the the "xml" file is valid
// 2) assigns a MagitRepository data member - parsing from the XML file
// 3) assign a Repository data member - parsing from the MagitRepository dataMember
// 4) to get a valid Repository - a)CreateRepositoryFromXml(path to file) -> b)getRepository();
public class XMLMain
{
    private static String XML_OBJECTS = "XmlObjects";

    private XMLValidate m_XmlValidate = new XMLValidate();
    private XMLParser m_XmlParser = new XMLParser();
    private Repository m_ParsedRepository = null;
    private MagitRepository m_XmlRepository = null;

    public XMLMain()
    {
    }

    public void setXmlRepositoryInXMLParser(MagitRepository i_XmlRepository)
    {
        this.m_XmlRepository = i_XmlRepository;
    }

    public boolean CheckXMLFile(String xmlContent) throws Exception
    {
        boolean isXMLRepoAlreadyExist;

        //m_XmlValidate.checkExistencesAndXMLExtension(i_XmlFilePath);

        m_XmlRepository = parseFromXmlFileToXmlMagitRepository(xmlContent);
        m_XmlValidate.setAllObjects(m_XmlRepository);

        m_XmlValidate.validateXmlRepositoryAndAssign(this);
        isXMLRepoAlreadyExist = checkIfAnotherRepoInLocation();

        return isXMLRepoAlreadyExist;
    }

    public MagitRepository getXmlRepository()
    {
        return m_XmlRepository;
    }

    public Repository ParseAndWriteXML(MagitRepository i_MagitRepository, String currentUserName) throws Exception
    {
        m_XmlParser.setAllObjects(i_MagitRepository);

        boolean isLocalRepository = IsLocalRepository(i_MagitRepository);

        if (isLocalRepository)
        // only if it is valid we continue to create an Repository Object
        {
            m_ParsedRepository = m_XmlParser.ParseLocalRepositoryFromXmlFile(currentUserName);

            Engine.initNewPaths(m_ParsedRepository.getRepositoryPath(), m_ParsedRepository.getAllCommitsSHA1ToCommit().values());

            LocalRepositoryWriter writer = new LocalRepositoryWriter((LocalRepository) m_ParsedRepository);
            writer.WriteRepositoryToFileSystem(m_ParsedRepository.getActiveBranch().getBranchName());
        } else
        {
            m_ParsedRepository = m_XmlParser.ParseRepositoryFromXmlFile(currentUserName);

            Engine.initNewPaths(m_ParsedRepository.getRepositoryPath(), m_ParsedRepository.getAllCommitsSHA1ToCommit().values());

            RepositoryWriter writer = new RepositoryWriter(m_ParsedRepository);
            writer.WriteRepositoryToFileSystem(m_ParsedRepository.getActiveBranch().getBranchName());
        }

        return m_ParsedRepository;
    }

    public boolean IsLocalRepository(MagitRepository magitRepository)
    {
        if (magitRepository.magitRemoteReference == null)
            return false;

        return magitRepository.magitRemoteReference.location != null;
    }


    private MagitRepository parseFromXmlFileToXmlMagitRepository(Path i_pathToXmlRepository) throws JAXBException
    {
        MagitRepository XmlRepositoryToValidate = null;
        JAXBContext jaxbContext;
        Unmarshaller unmarshaller;

        jaxbContext = JAXBContext.newInstance(MagitRepository.class);
        unmarshaller = jaxbContext.createUnmarshaller();
        XmlRepositoryToValidate = (MagitRepository) unmarshaller.unmarshal(i_pathToXmlRepository.toFile());

        return XmlRepositoryToValidate;
    }

    private MagitRepository parseFromXmlFileToXmlMagitRepository(String i_XMLContent) throws JAXBException, FileNotFoundException
    {
        InputStream inputStream = new ByteArrayInputStream(i_XMLContent.getBytes());
        JAXBContext jc = JAXBContext.newInstance(XML_OBJECTS);
        Unmarshaller u = jc.createUnmarshaller();

        return (MagitRepository) u.unmarshal(inputStream);
    }

    //todo:
    // reduce function using stream
    private boolean checkIfAnotherRepoInLocation()
    {

        if (!Folder.IsFileExist(m_XmlRepository.getLocation()))
            return false;

        File[] filesInRepo = new File(m_XmlRepository.location).listFiles();
        //if there is such file, check if it is repository

        for (File currentFile : filesInRepo)
        {
            if (IsMagitFolder(currentFile))
                return true;
        }

        return false;
    }
}