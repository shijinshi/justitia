package cn.shijinshi.fabricmanager.service.utils.file;

import cn.shijinshi.fabricmanager.service.utils.file.exception.YamlToPojoException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

@Component
public class YamlFileUtils extends FileUtils{
    public Object readYamlFile(String file) throws FileNotFoundException {
        Yaml yaml = new Yaml();
        return yaml.load(new FileInputStream(file));
    }

    public Map readYamlFileAsMap(String file) throws FileNotFoundException {
        return (Map) readYamlFile(file);
    }

    public <T> T readYamlFile(String file, Class<T> type) throws YamlToPojoException {
        Yaml yaml = new Yaml();
        try {
            return yaml.loadAs(new FileInputStream(file), type);
        } catch (Exception e) {
            throw new YamlToPojoException(e.getMessage());
        }
    }

    public void writeYamlFile(Object data, String path) throws IOException {
        Yaml yaml = new Yaml();
        FileWriter fileWriter = new FileWriter(path);

        String dataStr = "";
        if (data instanceof Map)  {
            dataStr = yaml.dump(data);
        } else {
            if (data != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonStr = objectMapper.writeValueAsString(data);
                Map map = objectMapper.readValue(jsonStr, Map.class);
                dataStr = yaml.dump(map);
            }
        }

        fileWriter.write(dataStr);
        fileWriter.close();
    }
}
