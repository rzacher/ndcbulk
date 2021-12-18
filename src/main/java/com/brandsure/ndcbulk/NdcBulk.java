package com.brandsure.ndcbulk;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.opencsv.*;
import com.brandsure.ndc.*;

public class NdcBulk {

    HashMap<String, String> fdaLabelerToNdcMap = new HashMap<>();

    public void run() {
        try {
            // Read in the CSV file with the list of NDC's into a hashmap
            // using fda labeler code as the key
            readNDCList();

            // For each key, look up the company info
            lookupCompanyInfo();

            // Write the company info to a file.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // For each FDA LabelerCode, use the associated ndc to lookup the company info.
    private void lookupCompanyInfo() {
        System.out.println(fdaLabelerToNdcMap.values().size());

        NDCRestClient ndcRestClient = new NDCRestClient();

        int count = 0;
        for (String ndcShort : fdaLabelerToNdcMap.values()) {
            String fullNdc = null;
            if (ndcShort.length() == 9) {
                fullNdc = ndcShort + "-01";
            } else if  (ndcShort.length() == 10)  {
                fullNdc = ndcShort + "-1";
            } else {
                System.out.println("Error - unexpected length of ndc " + ndcShort);
                continue;
            }
            System.out.println(fullNdc);

            count++;
            if (count > 4) {
                break;
            } else {
                String[] args = {fullNdc, "/tmp"};
                String output = ndcRestClient.run(args);
                System.out.println(output);
            }
        }
    }

    // Add a an fdaLabelerCode and ndc in HashMap if not already there.
    // We only need one ndc per LabelerCode
    private void readNDCList() throws Exception {
        String productNdcFilePath = "C:\\code\\src\\brandsure\\ndcbulk\\product.csv";
        Reader reader = new FileReader(productNdcFilePath);
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(',')
                .withIgnoreQuotations(true)
                .build();

        CSVReader csvReader = new CSVReaderBuilder(reader)
                .withSkipLines(0)
                .withCSVParser(parser)
                .build();


        boolean isFirstLine = true;
        String[] nextLine;
        try {
            while ((nextLine = csvReader.readNext()) != null) {
               // System.out.println(nextLine);
                if (isFirstLine) {
                    isFirstLine = false;
                } else {
                    String ndcPackageCode = null;
                    try {
                        ndcPackageCode = nextLine[1];
                    } catch (Exception e) {
                        if (ndcPackageCode != null) {
                            System.out.println(ndcPackageCode);
                        }
                        continue;
                    }
                    String[] ndcParts = ndcPackageCode.split("-");
                    String fdaLabelerCode = ndcParts[0];
                    // Add a an fdaLabelerCode and ndc in HashMap if not already there.
                    // We only need one ndc per LabelerCode
                    if (!fdaLabelerToNdcMap.containsKey(fdaLabelerCode)) {
                        fdaLabelerToNdcMap.put(fdaLabelerCode, ndcPackageCode);
                    }
                }
            }
            reader.close();
            csvReader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    public static void main(String[] args)  {
        NdcBulk ndcBulk = new NdcBulk();
        ndcBulk.run();
    }

}
