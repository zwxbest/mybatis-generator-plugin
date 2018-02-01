/**
 *    Copyright ${license.git.copyrightYears} the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.internal;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.config.JavaClientGeneratorConfiguration;
import org.mybatis.generator.exception.ShellException;


/**
 * The Class DefaultShellCallback.
 *
 * @author Jeff Butler
 */
public class DefaultShellCallback implements ShellCallback {

    /** The overwrite. */
    private boolean overwrite;

    /**
     * Instantiates a new default shell callback.
     *
     * @param overwrite
     *            the overwrite
     */
    public DefaultShellCallback(boolean overwrite) {
        super();
        this.overwrite = overwrite;
    }

    /* (non-Javadoc)
     * @see org.mybatis.generator.api.ShellCallback#getDirectory(java.lang.String, java.lang.String)
     */
    @Override
    public File getDirectory(String targetProject, String targetPackage)
            throws ShellException {
        // targetProject is interpreted as a directory that must exist
        //
        // targetPackage is interpreted as a sub directory, but in package
        // format (with dots instead of slashes). The sub directory will be
        // created
        // if it does not already exist

        File project = new File(targetProject);
        if (!project.isDirectory()) {
            throw new ShellException(getString("Warning.9", //$NON-NLS-1$
                    targetProject));
        }

        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(targetPackage, "."); //$NON-NLS-1$
        while (st.hasMoreTokens()) {
            sb.append(st.nextToken());
            sb.append(File.separatorChar);
        }

        File directory = new File(project, sb.toString());
        if (!directory.isDirectory()) {
            boolean rc = directory.mkdirs();
            if (!rc) {
                throw new ShellException(getString("Warning.10", //$NON-NLS-1$
                        directory.getAbsolutePath()));
            }
        }

        return directory;
    }

    /* (non-Javadoc)
     * @see org.mybatis.generator.api.ShellCallback#refreshProject(java.lang.String)
     */
    @Override
    public void refreshProject(String project) {
        // nothing to do in the default shell callback
    }

    /* (non-Javadoc)
     * @see org.mybatis.generator.api.ShellCallback#isMergeSupported()
     */
    @Override
    public boolean isMergeSupported() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.mybatis.generator.api.ShellCallback#isOverwriteEnabled()
     */
    @Override
    public boolean isOverwriteEnabled() {
        return overwrite;
    }


    /* (non-Javadoc)
     * @see org.mybatis.generator.api.ShellCallback#mergeJavaFile(java.lang.String, java.lang.String, java.lang.String[], java.lang.String)
     */
    @Override
    public String mergeJavaFile(String newFileSource,
            File existingFile, String[] javadocTags, String fileEncoding)
            throws ShellException {
        throw new UnsupportedOperationException();
    }

    //将已存在的写入
    @Override
    public String mergeJavaFile(CompilationUnit unit, File existingFile) throws ShellException
    {

        try {
            if (!existingFile.exists())
                return unit.getFormattedContent();
            com.github.javaparser.ast.CompilationUnit newUnit=JavaParser.parse(unit.getFormattedContent());
            com.github.javaparser.ast.CompilationUnit oldUnit=JavaParser.parse(existingFile);


        com.github.javaparser.ast.NodeList<com.github.javaparser.ast.body.TypeDeclaration<?>> newTypes = newUnit.getTypes();
        com.github.javaparser.ast.NodeList<com.github.javaparser.ast.body.TypeDeclaration<?>> oldTypes = oldUnit.getTypes();
         if(oldTypes.size()==0)
                return unit.getFormattedContent();
            //合并imports
            MergeImports(oldUnit.getImports(),newUnit.getImports());
        //先考虑一个文件只有一个类
        //合并method
            NodeList<BodyDeclaration<?>> newMmeber=newTypes.get(0).getMembers();
            NodeList<BodyDeclaration<?>> oldMmeber=oldTypes.get(0).getMembers();

//            List<MethodDeclaration> newMethodDeclarations = newTypes.get(0).getMembers().stream().filter(x -> x.getClass().equals(MethodDeclaration.class)).
//                    map(x -> (MethodDeclaration) x).collect(Collectors.toList());
//            List<MethodDeclaration> oldMethodDeclarations = oldTypes.get(0).getMembers().stream().filter(x -> x.getClass().equals(MethodDeclaration.class)).
//                    map(x -> (MethodDeclaration) x).collect(Collectors.toList());
            List<MethodDeclaration> newMethodDeclarations=new ArrayList<>();
            List<MethodDeclaration> oldMethodDeclarations=new ArrayList<>();
            List<FieldDeclaration> oldFieldDeclarations=new ArrayList<>();
            List<FieldDeclaration> newFieldDeclarations=new ArrayList<>();
            for(BodyDeclaration member:newMmeber )
            {
                if(member instanceof MethodDeclaration)
                {
                    newMethodDeclarations.add((MethodDeclaration)member);
                }
                else if(member instanceof  FieldDeclaration)
                {
                    newFieldDeclarations.add((FieldDeclaration)member);
                }
            }
            for(BodyDeclaration member:oldMmeber )
            {
                if(member instanceof MethodDeclaration)
                {
                    oldMethodDeclarations.add((MethodDeclaration)member);
                }
                else if(member instanceof  FieldDeclaration)
                {
                    oldFieldDeclarations.add((FieldDeclaration)member);
                }
            }
        MergeMethods(oldTypes.get(0).getMembers(),oldMethodDeclarations,newMethodDeclarations );
        MergeFiedls(oldTypes.get(0).getMembers(),oldFieldDeclarations,newFieldDeclarations );

            return oldUnit.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return "";
    }

    //清除同一个包下的多余imports,不兼容1.6,以后处理
//    public void cleanUpImports(com.github.javaparser.ast.CompilationUnit unit)
//    {
//        String packageName=unit.getPackageDeclaration().get().getName().toString();
//        NodeList<ImportDeclaration> imports=unit.getImports();
//        for(Iterator<ImportDeclaration> iit=imports.iterator();iit.hasNext();)
//        {
//            ImportDeclaration importDeclaration=iit.next();
//            if(packageName.equals(importDeclaration.getName().getQualifier().get().toString()))
//            {
//                iit.remove();
//            }
//        }
//    }

    //合并imports
    private void MergeImports(com.github.javaparser.ast.NodeList<ImportDeclaration> oldImports, com.github.javaparser.ast.NodeList<ImportDeclaration> newImports) {
        boolean findSameImport = false;
        for(int i=0;i<newImports.size();i++){
            ImportDeclaration newImportDeclaration=newImports.get(i);
            findSameImport = false;
            for(int j=0;j<oldImports.size();j++)
            {
                ImportDeclaration oldImportDeclaration=oldImports.get(j);
                if(newImportDeclaration.equals(oldImportDeclaration))
                {
                    System.out.println(newImportDeclaration.getName()+"重复Import");
                    findSameImport=true;
                    break;
                }
            }
            if(findSameImport==false)
            {
                //添加不存在的
                oldImports.add(newImportDeclaration);
            }
        }
    }
   private void  MergeFiedls(NodeList<BodyDeclaration<?>> allOldMethods,List<FieldDeclaration> oldFields, List<FieldDeclaration> newFields)
   {
       boolean findSameField;
       for(int i=0;i<newFields.size();i++)
       {
           findSameField=false;
           FieldDeclaration newField=newFields.get(i);
           for(int j=0;j<oldFields.size();j++)
           {
               FieldDeclaration oldField=oldFields.get(j);
               if(newField.equals(oldField))
               {
                   findSameField=true;
               }
           }
           if(findSameField==false)
           {
               allOldMethods.add(newField);
           }
       }
   }

    /**
     * 参照重载定义
     * 参数,方法名都相同则为同一方法
     *
     * @param oldMethods
     * @param newMethods
     */
    private void MergeMethods(NodeList<BodyDeclaration<?>> allOldMethods,List<MethodDeclaration> oldMethods, List<MethodDeclaration> newMethods) {
        boolean findSameMethod=false;
        for (int i=0;i<newMethods.size();i++) {
             findSameMethod=false;
            MethodDeclaration newMethod = newMethods.get(i);
            for (int j = 0; j < oldMethods.size(); j++) {
                MethodDeclaration oldMethod = oldMethods.get(j);
                //方法名相同
                if (newMethod.getName().toString().equals(oldMethod.getName().toString()))
                {
                    boolean sameParameters = true;
                    com.github.javaparser.ast.NodeList<Parameter> oldParameters = oldMethod.getParameters();
                    com.github.javaparser.ast.NodeList<Parameter>  newParameters = newMethod.getParameters();
                    //要求顺序
                    if (oldParameters.size() != newParameters.size()) {
                        sameParameters = false;//继续遍历oldmethod
                        continue;
                    }
                    for (int k = 0; k < oldParameters.size(); k++) {
                        //类型不匹配,
                        if (oldParameters.get(k).getType().toString().equals(newParameters.get(k).getType().toString()) == false) {
                            sameParameters = false;
                            break;
                        }
                    }
                    //返回值,名称,参数类相同,同一方法
                    if (sameParameters == true) {
                        findSameMethod=true;
                        System.out.println(newMethod.getName() + "重复Method");
                        break;//跳出oldmethod,继续遍历newmethod
                    }
                }
            }
            if(findSameMethod==false)
            {
                ;allOldMethods.add(newMethod);
            }
        }
    }



}
