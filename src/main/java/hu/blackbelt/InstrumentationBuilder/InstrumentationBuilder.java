package hu.blackbelt.InstrumentationBuilder;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class InstrumentationBuilder {

    private static final String NAME_OF_THE_XML_FILE_TO_BE_CREATED = "src/main/resources/extension-anakin-rest.xml";
    private static final String START_OF_XML_FILE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<extension xmlns=\"https://newrelic.com/docs/java/xsd/v1.0\"\n" +
            "\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "\txsi:schemaLocation=\"newrelic-extension extension.xsd \" name=\"extension-anakin-rest\"\n" +
            "\tversion=\"1.0\" enabled=\"true\">\n" +
            "\t<instrumentation>\n";
    private static final String LOCATION_OF_CLASSES = "/home/attila/work/anakin/anakin-rest/src/main/java";
    private static final String END_OF_XML_FILE = "\t</instrumentation>\n" +
            "</extension>";

    public static void main(String[] args) throws IOException {
        FileWriter fileWriter = new FileWriter(NAME_OF_THE_XML_FILE_TO_BE_CREATED);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.printf(START_OF_XML_FILE);

        ArrayList<File> files = new ArrayList<>();
        listf(LOCATION_OF_CLASSES, files);

        for (File file : files) {
            FileInputStream in = new FileInputStream(file.getAbsolutePath());

            // parse the file
            CompilationUnit cu = JavaParser.parse(in);

            NodeList<TypeDeclaration<?>> types = cu.getTypes();

            System.out.println("Class name: " + types.get(0).getName());
            System.out.println("Package Name: " + cu.getPackageDeclaration().get().getName());

            List<String> methodnames = new ArrayList<>();
            boolean hasMethods = false;
            for (TypeDeclaration<?> type : types) {
                NodeList<BodyDeclaration<?>> members = type.getMembers();
                if (members != null) {
                    for (BodyDeclaration member : members) {
                        if (member.isMethodDeclaration()) {
                            hasMethods = true;
                            MethodDeclaration field = (MethodDeclaration) member;
                            System.out.println("Method name: " + field.getNameAsString());
                            methodnames.add(field.getNameAsString());
                        }
                    }
                }
            }

            if (hasMethods) {
                printWriter.printf("\t\t<pointcut transactionStartPoint=\"true\"\n" +
                        "\t\t\texcludeFromTransactionTrace=\"false\" ignoreTransaction=\"false\">\n" +
                        "\t\t\t<className>" + cu.getPackageDeclaration().get().getName() + "." + types.get(0).getName() + "</className>\n");
                for (String methodName : methodnames) {
                    printWriter.printf("\t\t\t<method>\n" +
                            "\t\t\t\t<name>" + methodName + "</name>\n" +
                            "\t\t\t</method>\n");
                }
                printWriter.printf("\t\t</pointcut>\n\n");
            }
            System.out.println("---------------------");
        }
        printWriter.printf(END_OF_XML_FILE);
        printWriter.close();
    }

    private static void listf(String directoryName, List<File> files) {
        // get directory
        File directory = new File(directoryName);

        // Get all files from a directory.
        File[] fList = directory.listFiles();
        if (fList != null) {
            for (File file : fList) {
                if (file.isFile() && file.getName().endsWith(".java")) {
                    files.add(file);
                } else if (file.isDirectory()) {
                    listf(file.getAbsolutePath(), files);
                }
            }
        }
    }
}
