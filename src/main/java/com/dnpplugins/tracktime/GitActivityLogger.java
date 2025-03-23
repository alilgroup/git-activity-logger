package com.dnpplugins.tracktime;

import com.intellij.openapi.application.ApplicationActivationListener;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.diagnostic.Logger;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

import java.awt.*;
import java.awt.event.MouseEvent;

import java.util.Timer;
import java.util.concurrent.TimeUnit;


public final class GitActivityLogger implements ProjectActivity {
    private static final long LOG_INTERVAL_MS = 5 * 60 * 1000; // 5 минут
    private final Timer timer = new Timer(true);
    private final Path logFilePath;
    private static final Logger LOG = Logger.getInstance(GitActivityLogger.class);
    final Debouncer debouncer = new Debouncer();
    private static Boolean isMouseActive = false;

    public GitActivityLogger() {
        String homeDir = System.getProperty("user.home");
        java.util.Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        String fileName = String.format("%d.%d.csv", year, month);
        logFilePath = Paths.get(homeDir, ".git-activity-logger", fileName);

        //Create file
        initFile();

        //Start mouse tracking
        initMouseListener();
    }

    private void initFile(){
        try {
            Files.createDirectories(logFilePath.getParent());
            Files.createFile(logFilePath);
            try (FileWriter writer = new FileWriter(logFilePath.toFile(), true)) {
                writer.write("Project name;Datetime (ISO);Branch name\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException ignored) {}
    }

    private void initMouseListener(){
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (event instanceof MouseEvent mouseEvent) {
                if (mouseEvent.getID() == MouseEvent.MOUSE_MOVED || mouseEvent.getID() == MouseEvent.MOUSE_DRAGGED) {
                    mouseHandler();
                }
            }
        }, AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }

    private void mouseHandler(){
        debouncer.debounce(Void.class, new Runnable() {
            @Override public void run() {
                isMouseActive = true;
            }
        }, 2, TimeUnit.MINUTES);
    }

    private void logGitBranch(Project project) {
        String branchName = getCurrentBranch(project);
        String timestampString = Instant.now().toString();
        String logEntry = String.format("%s;%s;%s\n",
                project.getName(), timestampString, branchName);
        try (FileWriter writer = new FileWriter(logFilePath.toFile(), true)) {
            writer.write(logEntry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getCurrentBranch(Project project) {
        GitRepositoryManager repoManager = GitRepositoryManager.getInstance(project);
        for (GitRepository repo : repoManager.getRepositories()) {
            if (repo.getCurrentBranch() != null) {
                return repo.getCurrentBranch().getName();
            }
        }
        return "unknown";
    }

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (Listener.isActive && isMouseActive) {
                    logGitBranch(project);
                    isMouseActive = false;
                }
            }
        }, 0, LOG_INTERVAL_MS);
        return null;
    }

    @Service
    public static final class Listener implements ApplicationActivationListener {
        public static boolean isActive = false;

            @Override
        public void applicationActivated(@NotNull IdeFrame ideFrame) {
            isActive = true;
        }

        @Override
        public void applicationDeactivated(@NotNull IdeFrame ideFrame) {
            isActive = false;
        }
    }
}