package groupfour.trafficsim.sim;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A helper class for parsing XML SUMO project files.
 *
 * @author 8wf92323f
 */
public class RouteParser {
    public static final Logger LOGGER = LogManager.getLogger(RouteParser.class.getName());

    /**
     * Parses an XML file.
     *
     * @param file a xml file
     * @return a readable document object
     */
    private static Document parseXML(File file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(file);
    }


    /**
     * Reads a sumo config file and fetches all referenced route files.
     * Each route file is read and routes are extracted and stored as
     * individual SumoRoute objects.
     */
    public static List<SumoRoute> parseRoutes(String configFilePath, Simulation simulation) throws RuntimeException {
        List<SumoRoute> routes = new ArrayList<>();

        try {
            File configFile = new File(configFilePath);
            List<String> routeFilePaths = RouteParser.parseRouteFilePaths(configFile);

            Path directory = configFile.toPath().getParent();

            for (String routeFilePath : routeFilePaths) {
                File file = directory.resolve(routeFilePath).toFile();

                if (!file.exists()) {
                    LOGGER.error(".sumocfg references non-existing route file: {}", routeFilePath);
                    continue;
                }

                routes.addAll(RouteParser.parseRouteFile(file, simulation));
            }
        } catch (Exception exception) {
            throw new RuntimeException("Exception whilst loading route files", exception);
        }

        return routes;
    }

    /**
     * @return a list of all referenced route files in the sumo config file.
     */
    private static List<String> parseRouteFilePaths(File configFile) throws Exception {
        List<String> paths = new ArrayList<>();
        Document document = RouteParser.parseXML(configFile);
        NodeList sumoConfigurationNodes = document.getElementsByTagName("sumoConfiguration");

        for (int i = 0; i < sumoConfigurationNodes.getLength(); ++i) {
            Element sumoConfigurationNode = (Element)sumoConfigurationNodes.item(i);
            NodeList inputNodes = sumoConfigurationNode.getElementsByTagName("input");

            for (int j = 0; j < inputNodes.getLength(); ++j) {
                Element inputNode = (Element)inputNodes.item(j);
                NodeList routeFileNodes = inputNode.getElementsByTagName("route-files");

                for (int k = 0; k < routeFileNodes.getLength(); ++k) {
                    Element routeFileNode = (Element)routeFileNodes.item(k);

                    String filePathsAttribute = routeFileNode.getAttribute("value").trim();
                    String[] filePaths = filePathsAttribute.split(",");

                    for (String filePath : filePaths) {
                        paths.add(filePath.trim());
                    }
                }
            }
        }

        return paths;
    }

    /**
     * @return a list of all routes referenced in the route file
     */
    private static List<SumoRoute> parseRouteFile(File routeFile, Simulation simulation) throws Exception {
        List<SumoRoute> routes = new ArrayList<>();
        Document document = RouteParser.parseXML(routeFile);

        NodeList routesNodes = document.getElementsByTagName("routes");

        for (int i = 0; i < routesNodes.getLength(); ++i) {
            Element routesNode = (Element)routesNodes.item(i);
            NodeList routeNodes = routesNode.getElementsByTagName("route");

            for (int j = 0; j < routeNodes.getLength(); ++j) {
                Element routeNode = (Element)routeNodes.item(j);

                String routeIdAttribute = routeNode.getAttribute("id");
                String edgesAttribute = routeNode.getAttribute("edges");
                String[] edgeIds = edgesAttribute.split("\\s+");

                SumoRoute sumoRoute = new SumoRoute(routeIdAttribute, edgeIds, simulation);
                routes.add(sumoRoute);
            }
        }

        return routes;
    }
}
