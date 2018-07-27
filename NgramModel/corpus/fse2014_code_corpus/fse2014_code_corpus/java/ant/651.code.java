package org.apache.tools.ant.types.resolver;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.XMLCatalog;
import org.apache.tools.ant.types.ResourceLocation;
import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
public class ApacheCatalogResolver extends CatalogResolver {
    private XMLCatalog xmlCatalog = null;
    static {
        CatalogManager.getStaticManager().setIgnoreMissingProperties(true);
        System.getProperties().put("xml.catalog.className",
                                   ApacheCatalog.class.getName());
        CatalogManager.getStaticManager().setUseStaticCatalog(false);
    }
    public void setXMLCatalog(XMLCatalog xmlCatalog) {
        this.xmlCatalog = xmlCatalog;
    }
    public void parseCatalog(String file) {
        Catalog catalog = getCatalog();
        if (!(catalog instanceof ApacheCatalog)) {
            throw new BuildException("Wrong catalog type found: " + catalog.getClass().getName());
        }
        ApacheCatalog apacheCatalog = (ApacheCatalog) catalog;
        apacheCatalog.setResolver(this);
        try {
            apacheCatalog.parseCatalog(file);
        } catch (MalformedURLException ex) {
            throw new BuildException(ex);
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }
    public void addPublicEntry(String publicid,
                               String systemid,
                               URL base) {
        ResourceLocation dtd = new ResourceLocation();
        dtd.setBase(base);
        dtd.setPublicId(publicid);
        dtd.setLocation(systemid);
        xmlCatalog.addDTD(dtd);
    }
    public void addURIEntry(String uri,
                            String altURI,
                            URL base) {
        ResourceLocation entity = new ResourceLocation();
        entity.setBase(base);
        entity.setPublicId(uri);
        entity.setLocation(altURI);
        xmlCatalog.addEntity(entity);
    }
} 
