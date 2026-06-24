
//This class file is 1/4th vibecoded
import javax.swing.*;
import java.io.*;
import java.util.List;

public class GitManager {

    private final String repoPath;
    final String authUrl;

    public GitManager(String repoPath, String username, String token, String repoUrl)
            throws IOException, InterruptedException {
        this.repoPath = repoPath;
        this.authUrl = repoUrl.replace("https://",
                "https://" + username + ":" + token + "@");
        System.out.println(authUrl);
    }

    String runCommand(List<String> command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            File dir = new File(repoPath);
            if (dir.exists())
                pb.directory(dir);
            pb.redirectErrorStream(true);

            Process process = pb.start();
            StringBuilder output = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            process.waitFor();
            return output.toString().trim();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Delete a folder recursively haha cursive(im going insane)
    private void deleteFolder(File folder) {
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) deleteFolder(f);
                    else f.delete();
                }
            }
            folder.delete();
        }
    }

    // Clone repo, wiping repoPath first so no conflicts
    public void cloneRepo() {
        File dir = new File(repoPath);

        if (dir.exists()) {
            try {
                Process wipe = new ProcessBuilder("cmd", "/c", "rmdir", "/s", "/q", repoPath)
                        .redirectErrorStream(true)
                        .start();
                wipe.waitFor();
                Thread.sleep(300);
                System.out.println("Wiped: " + repoPath);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        File parent = dir.getParentFile();
        String folderName = dir.getName();
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    List.of("git", "clone", authUrl, folderName));
            pb.directory(parent);
            pb.redirectErrorStream(true);

            Process process = pb.start(); //start clone
            StringBuilder output = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            process.waitFor();
            System.out.println("Cloned:"+ repoPath);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    // Pull and force overwrite all local files
    public String pull() {
        runCommand(List.of("git", "fetch", "--all"));
        return runCommand(List.of("git", "reset", "--hard", "origin/main"));
    }

    public String lastCommitTime() {
        return runCommand(List.of("git", "log", "-1", "--format=by %cn on %cd", "--date=format:%Y-%m-%d %H:%M:%S"));
    }

    public String addAll() {
        runCommand(List.of("git", "add", "-A")); // -A catches new, modified AND deleted files
        return runCommand(List.of("git", "status"));
    }

    public String commit(String message) {
        return runCommand(List.of("git", "commit", "-m", message));
    }

    public String push() {
        return runCommand(List.of("git", "push", authUrl));
    }

    public String status() {
        return runCommand(List.of("git", "status"));
    }

    public String branch() {
        return runCommand(List.of("git", "branch", "--show-current"));
    }

    public void pushAll(String commitMessage) {
        System.out.println(addAll());
        System.out.println(commit(commitMessage));
        System.out.println(push());
        JOptionPane.showMessageDialog(null,"Done");
    }
}