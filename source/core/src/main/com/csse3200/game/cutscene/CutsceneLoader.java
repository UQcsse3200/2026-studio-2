package com.csse3200.game.cutscene;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Loads and validates cutscenes from the internal assets directory. */
public class CutsceneLoader {
  private static final String ROOT = "images/cutscenes/";
  private static final String MANIFEST = "config.json";

  private final Json json;

  public CutsceneLoader() {
    this(new Json());
  }

  CutsceneLoader(Json json) {
    this.json = json;
  }

  /** Loads a cutscene by name. Returns a result containing an error instead of throwing. */
  public Result load(String name) {
    if (!isValidName(name)) {
      return Result.error("Invalid cutscene name");
    }

    FileHandle directory = com.badlogic.gdx.Gdx.files.internal(ROOT + name);
    if (!directory.exists() || !directory.isDirectory()) {
      return Result.error("Cutscene directory does not exist: " + name);
    }

    FileHandle manifest = directory.child(MANIFEST);
    CutsceneDefinition definition;
    try {
      definition =
          manifest.exists()
              ? json.fromJson(CutsceneDefinition.class, manifest)
              : fallback(directory);
    } catch (Exception exception) {
      return Result.error("Could not parse cutscene manifest: " + exception.getMessage());
    }

    if (definition == null || definition.scenes == null || definition.scenes.isEmpty()) {
      return Result.error("Cutscene contains no scenes: " + name);
    }

    List<String> imagePaths = new ArrayList<>();
    for (CutsceneScene scene : definition.scenes) {
      if (scene == null || scene.image == null || scene.image.isBlank()) {
        return Result.error("Cutscene contains a scene without an image");
      }
      if (!isSafeRelativePath(scene.image)) {
        return Result.error("Invalid cutscene image path: " + scene.image);
      }
      FileHandle image = directory.child(scene.image);
      if (!isJpeg(image) || !image.exists() || image.isDirectory()) {
        return Result.error("Missing or unsupported cutscene image: " + scene.image);
      }
      imagePaths.add(image.path());
    }

    if (definition.fadeDuration <= 0f) {
      definition.fadeDuration = 0.4f;
    }
    return Result.success(new LoadedCutscene(name, definition, imagePaths.toArray(new String[0])));
  }

  private CutsceneDefinition fallback(FileHandle directory) {
    CutsceneDefinition definition = new CutsceneDefinition();
    for (FileHandle file : directory.list()) {
      if (isJpeg(file)) {
        CutsceneScene scene = new CutsceneScene();
        scene.image = file.name();
        scene.text.pages = new ArrayList<>();
        definition.scenes.add(scene);
      }
    }
    definition.scenes.sort(Comparator.comparing(scene -> scene.image.toLowerCase()));
    return definition;
  }

  private static boolean isJpeg(FileHandle file) {
    String extension = file.extension().toLowerCase();
    return extension.equals("jpg") || extension.equals("jpeg");
  }

  private static boolean isValidName(String name) {
    return name != null
        && !name.isBlank()
        && !name.startsWith("/")
        && !name.contains("..")
        && !name.contains("/")
        && !name.contains("\\");
  }

  private static boolean isSafeRelativePath(String path) {
    return !path.startsWith("/")
        && !path.startsWith("\\")
        && !path.contains("..")
        && !path.contains(":");
  }

  public static class Result {
    private final LoadedCutscene cutscene;
    private final String error;

    private Result(LoadedCutscene cutscene, String error) {
      this.cutscene = cutscene;
      this.error = error;
    }

    static Result success(LoadedCutscene cutscene) {
      return new Result(cutscene, null);
    }

    static Result error(String error) {
      return new Result(null, error);
    }

    public boolean isSuccess() {
      return cutscene != null;
    }

    public LoadedCutscene getCutscene() {
      return cutscene;
    }

    public String getError() {
      return error;
    }
  }

  public static class LoadedCutscene {
    private final String name;
    private final CutsceneDefinition definition;
    private final String[] imagePaths;

    public LoadedCutscene(String name, CutsceneDefinition definition, String[] imagePaths) {
      this.name = name;
      this.definition = definition;
      this.imagePaths = imagePaths;
    }

    public String getName() {
      return name;
    }

    public CutsceneDefinition getDefinition() {
      return definition;
    }

    public String[] getImagePaths() {
      return imagePaths.clone();
    }
  }
}
