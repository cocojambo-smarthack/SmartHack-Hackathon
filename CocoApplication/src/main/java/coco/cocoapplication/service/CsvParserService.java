package coco.cocoapplication.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CsvParserService {
    <T> List<T> parseCsv(String filePath, Class<T> clazz);
}