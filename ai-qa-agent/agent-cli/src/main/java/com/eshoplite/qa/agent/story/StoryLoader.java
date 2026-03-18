package com.eshoplite.qa.agent.story;

import com.eshoplite.qa.agent.model.AcceptanceCriterion;
import com.eshoplite.qa.agent.model.Story;
import org.apache.commons.io.FileUtils;
import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Very simple loader that expects a fenced YAML block in the markdown with keys:
 * id, title, description, ac: [ { name, given, when, then } ]
 */
public class StoryLoader {
    public static Story load(String path) throws Exception {
        String md = FileUtils.readFileToString(new File(path), StandardCharsets.UTF_8);
        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        Document doc = parser.parse(md);
        String yamlText = null;
        for (Node n = doc.getFirstChild(); n != null; n = n.getNext()) {
            if (n instanceof FencedCodeBlock fcb) {
                String info = fcb.getInfo().toString();
                if (info.contains("yaml")) {
                    yamlText = fcb.getContentChars().toString();
                    break;
                }
            }
        }
        if (yamlText == null) throw new IllegalStateException("No YAML fenced block found in story markdown: " + path);
        Map<String,Object> map = new Yaml().load(yamlText);
        String id = Objects.toString(map.get("id"), "US-UNKNOWN");
        String title = Objects.toString(map.get("title"), "Untitled");
        String desc = Objects.toString(map.get("description"), "");
        List<AcceptanceCriterion> acs = new ArrayList<>();
        List<Map<String,Object>> acList = (List<Map<String,Object>>) map.getOrDefault("ac", List.of());
        for (Map<String,Object> ac : acList) {
            acs.add(new AcceptanceCriterion(
                Objects.toString(ac.get("name"), "Unnamed"),
                Objects.toString(ac.get("given"), ""),
                Objects.toString(ac.get("when"), ""),
                Objects.toString(ac.get("then"), "")
            ));
        }
        return new Story(id, title, desc, acs);
    }
}
