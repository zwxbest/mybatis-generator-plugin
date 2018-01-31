package com.haitian;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User:zhangweixiao
 * Description:
 */
public class testRegex {



    @Test
    public void regexTest()
    {
        String str="<resultMap id=\"BaseResultMap\" type=\"com.haitian.generator.User\">\n" +
                "  <id column=\"id\" jdbcType=\"INTEGER\" property=\"id\" />\n" +
                "  <result column=\"author\" jdbcType=\"VARCHAR\" property=\"author\" />\n" +
                "  <result column=\"name\" jdbcType=\"VARCHAR\" property=\"name\" />\n" +
                "</resultMap>\n";
//        Pattern p=Pattern.compile("<([a-zA-Z0-9_]+)\\s+id=\"([a-zA-Z0-9_]+)\"");
        Pattern p=Pattern.compile("<(\\w+)\\s+id=\"(\\w+)\"");
        Matcher m=p.matcher(str);
        if(m.find())
        {
            System.out.println(m.group(1));
            System.out.println(m.group(2));
        }
        Assert.assertEquals(m.group(1),"resultMap");
        Assert.assertEquals(m.group(2),"BaseResultMap");
    }




}
