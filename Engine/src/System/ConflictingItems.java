package System;

import Objects.Blob;

public class ConflictingItems
{
    Blob m_OurBlob;
    Blob m_TheirBlob;
    Blob m_BaseVersionBlob;

    public ConflictingItems(Blob i_PullingItem, Blob i_PulledItem, Blob i_BaseVersionItem)
    {
        m_OurBlob = i_PullingItem;
        m_TheirBlob = i_PulledItem;
        m_BaseVersionBlob = i_BaseVersionItem;
    }

    public Blob getOurBlob()
    {
        return m_OurBlob;
    }

    public Blob getTheirBlob()
    {
        return m_TheirBlob;
    }

    public Blob getBaseVersionBlob()
    {
        return m_BaseVersionBlob;
    }

    public String getName()
    {
        if (m_OurBlob != null && !m_OurBlob.equals("null"))
        {
            return m_OurBlob.getName();
        } else if (m_TheirBlob != null && !m_TheirBlob.equals("null"))
            return m_TheirBlob.getName();
        else return null;
    }

    public Blob getBlobByContent(String blobText)
    {
        if (blobText.equals(m_OurBlob.getContent()))
            return m_OurBlob;

        return blobText.equals(m_TheirBlob.getContent()) ? m_TheirBlob : m_BaseVersionBlob;
    }
}
