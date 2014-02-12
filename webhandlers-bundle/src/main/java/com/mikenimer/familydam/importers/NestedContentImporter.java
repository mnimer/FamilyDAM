/*
 * This file is part of FamilyDAM Project.
 *
 *     The FamilyDAM Project is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     The FamilyDAM Project is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with the FamilyDAM Project.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mikenimer.familydam.importers;

import com.mikenimer.familydam.readers.NestedJsonReader;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.mime.MimeTypeService;
import org.apache.sling.jcr.contentloader.ContentImportListener;
import org.apache.sling.jcr.contentloader.ContentImporter;
import org.apache.sling.jcr.contentloader.ImportOptions;
import org.apache.sling.jcr.contentloader.internal.BaseImportLoader;
import org.apache.sling.jcr.contentloader.internal.ContentReader;
import org.apache.sling.jcr.contentloader.internal.DefaultContentCreator;
import org.apache.sling.jcr.contentloader.internal.DefaultContentImporter;
import org.apache.sling.jcr.contentloader.internal.ImportProvider;
import org.apache.sling.jcr.contentloader.internal.JcrContentHelper;
import org.apache.sling.jcr.contentloader.internal.readers.JsonReader;
import org.apache.sling.jcr.contentloader.internal.readers.XmlReader;
import org.apache.sling.jcr.contentloader.internal.readers.ZipReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.InvalidSerializedDataException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static javax.jcr.ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW;

/**
 * Created by mnimer on 2/10/14.
 */
@Component(metatype=true, immediate=false, label="%content.import.service.name", description="%content.import.service.description")
@Properties({
        @Property(name="service.vendor", value="The Apache Software Foundation"),
        @Property(name="service.description", value="Apache Sling JCR Content Import Service")
})
@Service(ContentImporter.class)
public class NestedContentImporter extends BaseImportLoader implements JcrContentHelper, ContentImporter
{

    /** default log */
    private final Logger log = LoggerFactory.getLogger(DefaultContentImporter.class);

    /**
     * The MimeTypeService used by the initial content initialContentLoader to
     * resolve MIME types for files to be installed.
     */
    @Reference
    private MimeTypeService mimeTypeService;

    public static final String EXT_JSON = ".json";

    /** All available import providers. */
    Map<String, ImportProvider> importProviders;


    public NestedContentImporter()
    {
        importProviders = new LinkedHashMap<String, ImportProvider>();
        importProviders.put(EXT_JCR_XML, null);
        importProviders.put(EXT_JSON, NestedJsonReader.PROVIDER);
        importProviders.put(EXT_XML, XmlReader.PROVIDER);
        importProviders.put(EXT_JAR, ZipReader.JAR_PROVIDER);
        importProviders.put(EXT_ZIP, ZipReader.ZIP_PROVIDER);

    }


    public NestedContentImporter(Map<String, ImportProvider> defaultImportProviders)
    {
        this.importProviders = defaultImportProviders;
    }


    @Override
    public void importContent(Node parent, String name, InputStream contentStream, ImportOptions importOptions, ContentImportListener importListener) throws RepositoryException, IOException
    {

        // special treatment for system view imports
        if (name.endsWith(EXT_JCR_XML)) {
            boolean replace = (importOptions == null)
                    ? false
                    : importOptions.isOverwrite();
            Node node = importSystemView(parent, name, contentStream, replace);
            if (node != null) {
                if (importListener != null) {
                    importListener.onCreate(node.getPath());
                }
                return;
            }
        }

        DefaultContentCreator contentCreator = new DefaultContentCreator(this);
        List<String> createdPaths = new ArrayList<String>();
        contentCreator.init(importOptions, importProviders, createdPaths, importListener);

        contentCreator.prepareParsing(parent, toPlainName(contentCreator, name));

        final ImportProvider ip = contentCreator.getImportProvider(name);
        ContentReader reader = ip.getReader();
        reader.parse(contentStream, contentCreator);

        //save changes
        Session session = parent.getSession();
        session.save();

        // finally checkin versionable nodes
        for (final Node versionable : contentCreator.getVersionables()) {
            versionable.checkin();
            if ( importListener != null ) {
                importListener.onCheckin(versionable.getPath());
            }
        }
    }




    private String toPlainName(DefaultContentCreator contentCreator, String name) {
        final String providerExt = contentCreator.getImportProviderExtension(name);
        if (providerExt != null) {
            if (name.length() == providerExt.length()) {
                return null; //no name is provided
            }
            return name.substring(0, name.length() - providerExt.length());
        }
        return name;
    }


    /**
     * Import the XML file as JCR system or document view import. If the XML
     * file is not a valid system or document view export/import file,
     * <code>false</code> is returned.
     *
     * @param parent The parent node below which to import
     * @param name the name of the import resource
     * @param contentStream The XML content to import
     * @param replace Whether or not to replace the subtree at name if the
     *    node exists.
     * @return <code>true</code> if the import succeeds, <code>false</code>
     *         if the import fails due to XML format errors.
     * @throws IOException If an IO error occurrs reading the XML file.
     */
    private Node importSystemView(Node parent, String name,
                                  InputStream contentStream, boolean replace) throws IOException {
        InputStream ins = null;
        try {

            // check whether we have the content already, nothing to do then
            final String nodeName = (name.endsWith(EXT_JCR_XML))
                    ? name.substring(0, name.length() - EXT_JCR_XML.length())
                    : name;

            // ensure the name is not empty
            if (nodeName.length() == 0) {
                throw new IOException("Node name must not be empty (or extension only)");
            }

            // check for existence/replacement
            if (parent.hasNode(nodeName)) {
                Node existingNode = parent.getNode(nodeName);
                if (replace) {
                    log.debug("importSystemView: Removing existing node at {}",
                            nodeName);
                    existingNode.remove();
                } else {
                    log.debug(
                            "importSystemView: Node {} for XML already exists, nothing to to",
                            nodeName);
                    return existingNode;
                }
            }

            ins = contentStream;
            Session session = parent.getSession();
            session.importXML(parent.getPath(), ins, IMPORT_UUID_CREATE_NEW);

            // additionally check whether the expected child node exists
            return (parent.hasNode(nodeName)) ? parent.getNode(nodeName) : null;
        } catch (InvalidSerializedDataException isde) {

            // the xml might not be System or Document View export, fall back
            // to old-style XML reading
            log.info(
                    "importSystemView: XML does not seem to be system view export, trying old style; cause: {}",
                    isde.toString());
            return null;
        } catch (RepositoryException re) {

            // any other repository related issue...
            log.info(
                    "importSystemView: Repository issue loading XML, trying old style; cause: {}",
                    re.toString());
            return null;
        } finally {
            if (ins != null) {
                try {
                    ins.close();
                } catch (IOException ignore) {
                    // ignore
                }
            }
        }
    }


    // ---------- JcrContentHelper implementation ---------------------------------------------

    /* (non-Javadoc)
     * @see org.apache.sling.jcr.contentloader.internal.JcrContentHelper#getMimeType(java.lang.String)
     */
    public String getMimeType(String name) {
        // local copy to not get NPE despite check for null due to concurrent
        // unbind
        MimeTypeService mts = mimeTypeService;
        return (mts != null) ? mts.getMimeType(name) : null;
    }

}
