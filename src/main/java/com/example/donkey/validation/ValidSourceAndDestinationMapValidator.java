package com.example.donkey.validation;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ValidSourceAndDestinationMapValidator implements ConstraintValidator<ValidSourceAndDestinationMap, Map<String, String>> {

    public static final String VALID = "Valid";
    @Value("${document.upload-directory}")
    private String uploadDirectory;
    private final String hasFileRegex = ".+\\.([^.]+)$";


    @Override
    public boolean isValid(Map<String, String> srcValueMap, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        List<Pair<String, String>> errors = new ArrayList<>();
        for(Map.Entry<String, String> entry : srcValueMap.entrySet()){

            String srcResponse = isValidSourceOrDestination(entry.getKey(), true);
            if(!srcResponse.equals(VALID)) errors.add(new ImmutablePair<>(entry.getKey(), srcResponse));

            String destResponse = isValidSourceOrDestination(entry.getKey(), false);
            if(!destResponse.equals(VALID)) errors.add(new ImmutablePair<>(entry.getKey(), destResponse));

            String entryResponse = pairIsValid(entry.getKey(), entry.getValue());
            if(!entryResponse.equals(VALID)) errors.add(new ImmutablePair<>(entry.getKey(), entryResponse));
        }

        errors.forEach(tuple -> context.buildConstraintViolationWithTemplate(tuple.getRight()).addPropertyNode(tuple.getLeft()).addConstraintViolation());

        return errors.isEmpty();
    }

    public String isValidSourceOrDestination(String path, boolean isSrc){
        if(path == null || path.isBlank()) return "Path is null or empty";
        if(path.contains("..")) return String.format("Path contains unsafe sequence(s). Path: %s", path);
        if(path.startsWith("/") || path.startsWith("\\") || path.startsWith("\\\\")) return String.format("Path starts with invalid symbol %c.", path.charAt(0));
        if(path.contains("/src/main/resources") || path.contains("classpath")) return String.format("Path: %s contains reference to classpath which is forbidden.", path);
        if(path.equals(uploadDirectory)) return String.format("Path contains the upload location. Path: %s", path);
        Path asPath = Path.of(path);
        boolean isFile = path.matches(hasFileRegex);
        if(asPath.isAbsolute()) return String.format("Path is not allowed to be absolute. Path: %s", path);
        if((isFile && asPath.getNameCount() -1  > 3) || (!isFile && asPath.getNameCount() > 3)) return String.format("Path has invalid depth, max depth: directories: %d and with a file: %d. Path: %s.",3, 4, path);
        Path fullPath = Path.of(uploadDirectory).resolve(path);
        if(isSrc && !fullPath.toFile().isFile()) return String.format("Source file doesn't exist. Path: %s", path);
        if(isSrc && !isFile && fullPath.toFile().isDirectory()) return String.format("Source path does not contain a directory at that location. Path: %s" ,path);
        return VALID;
    }

    public String pairIsValid(String src, String dest){
        if(src.matches(hasFileRegex) && !dest.matches(hasFileRegex)) {
            return "Invalid pair, file defined in source: " + src + "is not present in target: " + dest;
        }
        if(!src.matches(hasFileRegex) && dest.matches(hasFileRegex)){
            return "Invalid pair, directory defined in source: " + src + " is not applicable to target: " + dest;
        }
        return VALID;
    }

}
