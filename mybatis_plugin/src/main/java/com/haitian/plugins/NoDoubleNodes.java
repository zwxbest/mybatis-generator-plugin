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
 */
public class NoDoubleNodes extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    //mybatis_generator中类,用来根据TargetProject和TargetPackage生成File,自己写也不难.
    ShellCallback shellCallback=new DefaultShellCallback(false);
    //因为sqlMapDocumentGenerated先调用,要保存Document,用来在sqlMapGenerated中删除子node
    org.mybatis.generator.api.dom.xml.Document document;

    @Override
    public boolean sqlMapDocumentGenerated(org.mybatis.generator.api.dom.xml.Document document,
                                           IntrospectedTable introspectedTable) {
        this.document=document;
        return true;
    }

    @Override
    public boolean sqlMapGenerated(GeneratedXmlFile sqlMap,
                                   IntrospectedTable introspectedTable) {

        try {
            //将旧的xml mapper读取到Document中(注意这个Document和上面那个Document不是同一类型,上面那个是mybatis中的)
//            org.mybatis.generator.api.dom.xml.Document
            File directory = shellCallback.getDirectory(sqlMap.getTargetProject(), sqlMap.getTargetPackage());
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            File xmlFile=new File(directory, sqlMap.getFileName());
            if(directory.exists()==false||xmlFile.exists()==false)
                return true;
            Document doc = db.parse(new FileInputStream(xmlFile));
            org.w3c.dom.Element rootElement = doc.getDocumentElement();
            NodeList list = rootElement.getChildNodes();

            List<Element> elements = document.getRootElement().getElements();

            //从node表示的text中读取name和id属性的值,例如<sql id="insert">...</sql>
//            可读取到sql和insert
            Pattern p=Pattern.compile("<(\\w+)\\s+id=\"(\\w+)\"");

            boolean findSameNode=false;
//            遍历新的node,因为做删除操作,这里要用iterator,,
            for (Iterator<Element> elementIt = elements.iterator(); elementIt.hasNext();)
            {
                findSameNode=false;
                String newNodeName="";
                String NewIdValue="";
                Element element=elementIt.next();
                Matcher m=p.matcher(element.getFormattedContent(0));
                if(m.find())
                {
                    //获取新的node的name和id属性的值
                    newNodeName=m.group(1);
                    NewIdValue=m.group(2);
                }
                //遍历旧的node,如果name和id属性的值相同,那么定义为重复,不进行覆盖,从elements中删除
                for(int i=0;i<list.getLength();i++) {
                    Node node = list.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        if(newNodeName.equals(node.getNodeName()))
                        {
                            NamedNodeMap attr = node.getAttributes();
                            for (int j = 0; j < attr.getLength(); j++) {
                                Node attrNode = attr.item(j);
                                if (attrNode.getNodeName().equals("id") &&attrNode.getNodeValue().equals(NewIdValue)) {
                                    //name和ndoe属性值相等,删除并break出去,elementIt->document->新的mapper.xml
                                    elementIt.remove();
                                    findSameNode=true;
                                    break;
                                }
                            }
                            if(findSameNode==true)
                                break;
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return true;
    }

}
