
package com.haitian.plugins;

import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User:zhangweixiao
 * Description:
 * old nodes is your existing xml file's first level nodes,like <insert><resultMap>
 *  new nodes is mybatis-generator generate for you to combine
 * This compare the first level node's name and "id" attribute of new nodes and old nodes
 * if the two equal,then new node will not generate
 * so this can make you modification in old nodes not override.
 * if you want to regenrate old node,delete it,it will generate new.
 */
public class CombineXmlPlugin extends PluginAdapter {
    //shellCallback use TargetProject and TargetPackage to get targetFile
    ShellCallback shellCallback = new DefaultShellCallback(false);
    //save new nodes
    org.mybatis.generator.api.dom.xml.Document document;

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    /**
     * assing document variable to get new nodes
     * @param document
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean sqlMapDocumentGenerated(org.mybatis.generator.api.dom.xml.Document document,
                                           IntrospectedTable introspectedTable) {
        this.document = document;
        return true;
    }


    //new nodes is generated,but not write on disk,we just need to filter.
    @Override
    public boolean sqlMapGenerated(GeneratedXmlFile sqlMap,
                                   IntrospectedTable introspectedTable) {

        try {
            //get old nodes
            File directory = shellCallback.getDirectory(sqlMap.getTargetProject(), sqlMap.getTargetPackage());
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            File xmlFile = new File(directory, sqlMap.getFileName());
            if (directory.exists() == false || xmlFile.exists() == false)
                return true;
            Document doc = db.parse(new FileInputStream(xmlFile));
            org.w3c.dom.Element rootElement = doc.getDocumentElement();
            NodeList list = rootElement.getChildNodes();
            //get new nodes
            List<Element> elements = document.getRootElement().getElements();

            //get nodeName and the value of id attribute use regex
            Pattern p = Pattern.compile("<(\\w+)\\s+id=\"(\\w+)\"");

            boolean findSameNode = false;
            // traverse new nodes to compare old nodes to filter
            for (Iterator<Element> elementIt = elements.iterator(); elementIt.hasNext(); ) {
                findSameNode = false;
                String newNodeName = "";
                String NewIdValue = "";
                Element element = elementIt.next();
                Matcher m = p.matcher(element.getFormattedContent(0));
                if (m.find()) {
                    //get nodeName and the value of id attribute
                    newNodeName = m.group(1);
                    NewIdValue = m.group(2);
                }
                //if the nodeName of newNode and oldNode are equal
                //and the id attribute of newNode and oldNode are equal
                //then filter newNode
                for (int i = 0; i < list.getLength(); i++) {
                    Node node = list.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        if (newNodeName.equals(node.getNodeName())) {
                            NamedNodeMap attr = node.getAttributes();
                            for (int j = 0; j < attr.getLength(); j++) {
                                Node attrNode = attr.item(j);
                                if (attrNode.getNodeName().equals("id") && attrNode.getNodeValue().equals(NewIdValue)) {
                                    //filter new node,just delete it ,and it will not generate
                                    elementIt.remove();
                                    findSameNode = true;
                                    break;
                                }
                            }
                            if (findSameNode == true)
                                break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
    