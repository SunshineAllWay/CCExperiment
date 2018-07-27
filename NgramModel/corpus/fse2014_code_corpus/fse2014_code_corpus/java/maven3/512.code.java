package org.apache.maven.repository;
public interface ArtifactTransferResource
{
    String getRepositoryUrl();
    String getName();
    String getUrl();
    long getContentLength();
    long getTransferStartTime();
}
