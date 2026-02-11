package com.onetime.filelink;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RequestMapping("/api/files")
@RestController
public class FileController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    // token -> file info (in-memory)
    private final Map<String, FileInfo> fileStore = new HashMap<>();

    // ================= UPLOAD & GENERATE LINK =================
    @PostMapping("/upload")
    public Map<String, String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("expiry") int expiryMinutes) throws IOException {

        // ensure upload folder
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // generate unique token
        String token = UUID.randomUUID().toString();
        String fileName = token + "_" + file.getOriginalFilename();

        File destination = new File(dir, fileName);
        file.transferTo(destination);

        // expiry time
        long expiryTime = System.currentTimeMillis() + (expiryMinutes * 60 * 1000);

        // store token + file info
        fileStore.put(
        	    token,
        	    new FileInfo(
        	        destination.getAbsolutePath(),
        	        expiryTime,
        	        file.getContentType()
        	    )
        	);

        // ✅ IMPORTANT: view link (NOT download)
        Map<String, String> response = new HashMap<>();
        response.put("link", "http://localhost:8081/api/files/download/" + token);

        return response;
    }

    // ================= ONE-TIME OPEN & DOWNLOAD (VIEW ONLY) =================
    //-------------------Download/Open API fully dynamic-----------------------//
    @GetMapping("/download/{token}")
    public ResponseEntity<FileSystemResource> openOnce(@PathVariable String token) {

        FileInfo info = fileStore.get(token);

        if (info == null || System.currentTimeMillis() > info.getExpiryTime()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        File file = new File(info.getPath());

        if (!file.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // 🔥 one-time use
        fileStore.remove(token);

        MediaType mediaType = MediaType.parseMediaType(info.getContentType());

        return ResponseEntity.ok()
                .contentType(mediaType)   // 👈 MAGIC LINE
                .body(new FileSystemResource(file));
    }
}