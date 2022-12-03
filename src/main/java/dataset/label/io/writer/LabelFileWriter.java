package dataset.label.io.writer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.json.JSONException;
import org.json.JSONObject;

import dataset.bug.Log;
import dataset.label.Labeller;
import tregression.model.PairList;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.util.JavaUtil;
import tregression.separatesnapshots.DiffMatcher;
import tregression.tracematch.ControlPathBasedTraceMatcher;

public class LabelFileWriter {
    private final Logger logger = Log.createLogger(LabelFileWriter.class);
    private final String labelFile;
    private final Trace workingTrace;
    private final Trace buggyTrace;
    private final String workingProjectPath;
    private final String buggyProjectPath;
    private final String srcFolder;
    private final String testFolder;


    public LabelFileWriter(String labelFile, Trace workingTrace, Trace buggyTrace, String workingProjectPath,
            String buggyProjectPath, String srcFolder, String testFolder) {
        super();
        this.labelFile = labelFile;
        this.workingTrace = workingTrace;
        this.buggyTrace = buggyTrace;
        this.workingProjectPath = workingProjectPath;
        this.buggyProjectPath = buggyProjectPath;
        this.srcFolder = srcFolder;
        this.testFolder = testFolder;
        attachFilePathToTrace(workingTrace, String.join(File.separator, workingProjectPath, srcFolder), String.join(File.separator, workingProjectPath, testFolder));
        attachFilePathToTrace(buggyTrace, String.join(File.separator, buggyProjectPath, srcFolder), String.join(File.separator, buggyProjectPath, testFolder));
    }

    public boolean write() {
        logger.info("Creating diff matcher for label file " + labelFile);
        DiffMatcher diffMatcher = createDiffMatcher();
        logger.info("Creating pair list for label file " + labelFile);
        PairList pairList = createPairList(diffMatcher);
        Labeller labeller = new Labeller();
        logger.info("Assigning labels to buggy trace for label file " + labelFile);
        labeller.assignLabel(workingTrace, buggyTrace, pairList, diffMatcher);
        Map<String, Double> mapOfLabels = createMapOfVarIdToProbs();
        logger.info("Writing to label file " + labelFile);
        return write(mapOfLabels);
    }

    private boolean write(Map<String, Double> mapOfLabels) {
        File file = new File(labelFile);

        JSONObject contents = new JSONObject(mapOfLabels);
        try (FileWriter writer = new FileWriter(file)) {
            contents.write(writer);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private DiffMatcher createDiffMatcher() {
        DiffMatcher result = new DiffMatcher(srcFolder, testFolder, workingProjectPath, buggyProjectPath);
        result.matchCode();
        return result;
    }

    private PairList createPairList(DiffMatcher diffMatcher) {
        ControlPathBasedTraceMatcher traceMatcher = new ControlPathBasedTraceMatcher();
        return traceMatcher.matchTraceNodePair(buggyTrace, workingTrace, diffMatcher);
    }

    private Map<String, Double> createMapOfVarIdToProbs() {
        Map<String, Double> result = new HashMap<>();
        for (TraceNode node : buggyTrace.getExecutionList()) {
            for (VarValue var : node.getReadVariables()) {
                result.put(var.getVarID(), var.getProbability());
            }
            for (VarValue var : node.getWrittenVariables()) {
                result.put(var.getVarID(), var.getProbability());
            }
        }
        return result;
    }

    private void attachFilePathToTrace(Trace trace, String sourceFolder, String testFolder) {
        Map<String, String> map = new HashMap<>();
        for (TraceNode node : trace.getExecutionList()) {
            BreakPoint point = node.getBreakPoint();
            String declaringCompName = point.getDeclaringCompilationUnitName();
            if (map.containsKey(declaringCompName)) {
                point.setFullJavaFilePath(map.get(declaringCompName));
                continue;
            }
            String sourcePath = findDeclaringCompilationUnitName(sourceFolder + File.separator + getPackagePath(declaringCompName), getClassName(declaringCompName));
            String testPath = findDeclaringCompilationUnitName(testFolder + File.separator + getPackagePath(declaringCompName), getClassName(declaringCompName));
            if(new File(sourcePath).exists()) {
                point.setFullJavaFilePath(sourcePath);
                map.put(declaringCompName, sourcePath);
            }
            else if(new File(testPath).exists()) {
                point.setFullJavaFilePath(testPath);
                map.put(declaringCompName, testPath);
            }
            else {
                System.err.println("cannot find the source code file for " + point);
            }
        }
    }

    private static String findDeclaringCompilationUnitName(String packagePath, String canonicalClassName) {
        File possibleFile = new File(packagePath + File.separator + canonicalClassName + ".java");
        if (possibleFile.exists()) {
            return possibleFile.getAbsolutePath();
        }
        File packageFolder = new File(packagePath);

        if(!packageFolder.exists()){
            return "";
        }

        Collection<File> javaFiles = FileUtils.listFiles(packageFolder, new String[]{"java"}, false);;
        for(File javaFileObject: javaFiles){
            String javaFile = javaFileObject.getAbsolutePath();
            CompilationUnit cu = JavaUtil.parseCompilationUnit(javaFile);
            TypeNameFinder finder = new TypeNameFinder(cu, canonicalClassName);
            cu.accept(finder);
            if(finder.isFind){
                return javaFile;
            }
        }

        return "";
    }

    private String getPackagePath(String compilationUnitName) {
        int idxOfLastDot = compilationUnitName.lastIndexOf(".");
        return compilationUnitName.substring(0, idxOfLastDot).replace(".", File.separator);
    }

    private String getClassName(String compilationUnitName) {
        int idxOfLastDot = compilationUnitName.lastIndexOf(".");
        return compilationUnitName.substring(idxOfLastDot + 1);
    }

    static class TypeNameFinder extends ASTVisitor{
        CompilationUnit cu;
        boolean isFind = false;
        String canonicalClassName;

        public TypeNameFinder(CompilationUnit cu, String canonicalClassName) {
            super();
            this.cu = cu;
            this.canonicalClassName = canonicalClassName;
        }

        public boolean visit(TypeDeclaration type){
            String simpleName = canonicalClassName;
            if(canonicalClassName.contains(".")){
                simpleName = canonicalClassName.substring(
                        canonicalClassName.lastIndexOf(".")+1, canonicalClassName.length());
            }
            if(type.getName().getFullyQualifiedName().equals(simpleName)){
                this.isFind = true;
            }

            return false;
        }

    }
}
