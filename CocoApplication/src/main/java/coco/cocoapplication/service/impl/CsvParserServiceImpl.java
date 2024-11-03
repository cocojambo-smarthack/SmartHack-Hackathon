package coco.cocoapplication.service.impl;

import coco.cocoapplication.service.CsvParserService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class CsvParserServiceImpl implements CsvParserService {

    @Override
    public <T> List<T> parseCsv(String filePath, Class<T> clazz) {
        List<T> result = new ArrayList<>();
        try (Reader reader = new InputStreamReader(Objects.requireNonNull(CsvParserService.class.getClassLoader().getResourceAsStream("csv/" + filePath)));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withDelimiter(';').withFirstRecordAsHeader())) {
            // For every record in the CSV file populate the class fields
            for (CSVRecord csvRecord : csvParser) {
                T obj = clazz.getDeclaredConstructor().newInstance();
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    if (fieldName.equals("transits")) {
                        continue;
                    }
                    String fieldValue = csvRecord.get(fieldName);
                    if (field.getType().equals(String.class)) {
                        field.set(obj, fieldValue);
                    } else if (field.getType().equals(int.class)) {
                        field.set(obj, Integer.parseInt(fieldValue));
                    } else if (field.getType().equals(double.class)) {
                        field.set(obj, Double.parseDouble(fieldValue));
                    }
                }
                result.add(obj);
            }
        } catch (IOException | ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return result;
    }
}
