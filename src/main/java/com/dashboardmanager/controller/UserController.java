package com.dashboardmanager.controller;

import ca.odell.glazedlists.impl.io.BeanXMLByteCoder;
import cn.hutool.cache.file.LRUFileCache;
import com.dashboardmanager.model.Session;
import com.dashboardmanager.model.User;
import com.dashboardmanager.repository.SessionsRepository;
import com.dashboardmanager.repository.UsersRepository;
import com.dashboardmanager.utils.FileUtils;
import com.dashboardmanager.utils.SessionHeader;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.view.RedirectView;

import java.io.*;
import java.security.SecureRandom;
import java.util.List;

import static org.apache.commons.codec.binary.Base64.decodeBase64;

@RestController
public class UserController {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private SessionsRepository sessionsRepository;

    private LRUFileCache fileCache = new LRUFileCache(100);

    @GetMapping("/")
    public RedirectView index(HttpServletRequest request) {
        if (isAuthenticated(request)) {
            return new RedirectView("dashboard");
        }
        else {
            return new RedirectView("login");
        }
    }

    @PostMapping("/login")
    public String login(HttpServletResponse response, @RequestParam(value = "username") String username, @RequestParam(value = "password") String password) {
        User user = usersRepository.findByName(username);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (passwordEncoder.matches(password, user.getPassword())) {
            Session session = new Session();
            session.setUser(user);
            session.setSessionId(RandomStringUtils.random(20, 0, 0, true, true, null, new SecureRandom()));
            sessionsRepository.save(session);
            response.addHeader("Session-Auth", createSessionHeader(session));
            return "login successful";
        }
        else {
            return "invalid credentials!";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request) {
        if (isAuthenticated(request)) {
            User user = getUser(request);
            if (user == null) {
                return "an error occurred!";
            } else {
                return String.format("Welcome %s!", user.getName());
            }
        } else {
            return "forbidden!";
        }
    }

    @GetMapping("/user/images")
    public ResponseEntity<Resource> getUserImage(ServletWebRequest request, @RequestParam(value = "image_quality") String imageQuality) {
        try {
            String extension = "";
            switch (imageQuality) {
                case "scalable":
                    extension = ".svg";
                    break;
                case "high":
                    extension = ".png";
                    break;
                case "low":
                    extension = ".jpg";
                    break;
            }
            String imageFile = FileUtils.getInstance().getUserImagePath(request.getRemoteUser()) + extension;
            final ByteArrayResource inputStream = new ByteArrayResource(fileCache.getFileBytes(imageFile));
            return ResponseEntity.status(HttpStatus.OK).contentLength(inputStream.contentLength()).body(inputStream);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/user/import")
    public ResponseEntity<Resource> importUsers(HttpServletRequest request) {
        var coder = new BeanXMLByteCoder();
        List<User> newUsers;
        try {
             newUsers = (List<User>) coder.decode(request.getInputStream());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        usersRepository.saveAll(newUsers);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private String createSessionHeader(Session session) {
        SessionHeader sessionHeader = new SessionHeader(session.getUser().getName(), session.getSessionId());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(sessionHeader);
            oos.flush();
        } catch (IOException e) {
            return "";
        }
        return new String(org.apache.commons.codec.binary.Base64.encodeBase64(bos.toByteArray()));
    }

    private SessionHeader getSessionHeader(HttpServletRequest request) {
        String sessionAuth = request.getHeader("Session-Auth");
        if (sessionAuth != null) {
            try {
                byte[] decoded = decodeBase64(sessionAuth);
                ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(decoded));
                return (SessionHeader) in.readObject();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private boolean isAuthenticated(HttpServletRequest request) {
        SessionHeader sessionHeader = getSessionHeader(request);
        if (sessionHeader == null) return false;
        return sessionsRepository.findBySessionId(sessionHeader.getSessionId()) != null;
    }

    private User getUser(HttpServletRequest request) {
        SessionHeader sessionHeader = getSessionHeader(request);
        if (sessionHeader == null) return null;
        Session session = sessionsRepository.findBySessionId(sessionHeader.getSessionId());
        if (session != null) return session.getUser();
        return null;
    }
}
