package de.diedavids.mavguard.nexus.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Represents Maven metadata XML format.
 */
@XmlRootElement(name = "metadata")
@XmlAccessorType(XmlAccessType.FIELD)
public class MavenMetadata {

    @XmlElement(name = "groupId")
    private String groupId;

    @XmlElement(name = "artifactId")
    private String artifactId;

    @XmlElement(name = "versioning")
    private Versioning versioning;

    public MavenMetadata() {
        // Default constructor required by JAXB
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public Versioning getVersioning() {
        return versioning;
    }

    public void setVersioning(Versioning versioning) {
        this.versioning = versioning;
    }

    /**
     * Gets a list of all versions available for this artifact.
     *
     * @return list of available versions
     */
    public List<String> getVersions() {
        return versioning != null ? versioning.getVersions() : List.of();
    }

    /**
     * Gets the latest version available for this artifact.
     *
     * @return the latest version
     */
    public String getLatestVersion() {
        return versioning != null ? versioning.getLatest() : null;
    }

    /**
     * Gets the latest release version available for this artifact.
     *
     * @return the latest release version
     */
    public String getLatestReleaseVersion() {
        return versioning != null ? versioning.getRelease() : null;
    }

    /**
     * Versioning information in Maven metadata.
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Versioning {

        @XmlElement(name = "latest")
        private String latest;

        @XmlElement(name = "release")
        private String release;

        @XmlElement(name = "lastUpdated")
        private String lastUpdated;

        @XmlElementWrapper(name = "versions")
        @XmlElement(name = "version")
        private List<String> versions;

        public Versioning() {
            // Default constructor required by JAXB
        }

        public String getLatest() {
            return latest;
        }

        public void setLatest(String latest) {
            this.latest = latest;
        }

        public String getRelease() {
            return release;
        }

        public void setRelease(String release) {
            this.release = release;
        }

        public String getLastUpdated() {
            return lastUpdated;
        }

        public void setLastUpdated(String lastUpdated) {
            this.lastUpdated = lastUpdated;
        }

        public List<String> getVersions() {
            return versions != null ? versions : List.of();
        }

        public void setVersions(List<String> versions) {
            this.versions = versions;
        }
    }
}