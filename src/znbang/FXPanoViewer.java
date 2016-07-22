package znbang;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.io.File;

/**
 * FXPanoViewer is a spherical(equirectangular) panorama viewer based on JavaFX 3D.
 */
public class FXPanoViewer extends Application {
    private Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private double oldX;
    private double oldY;

    /**
     * Load image, scale to 8000x4000, flip horizontally.
     * @param file path to image file
     * @return a image
     */
    private Image loadImage(File file) {
        // load image and scale to 8000x4000
        Image image = new Image("file:" + file, 4000, 2000, true, true);

        // flip image horizontally
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-image.getWidth(), 0);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return SwingFXUtils.toFXImage(op.filter(SwingFXUtils.fromFXImage(image, null), null), null);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // create camera
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000);
        camera.setFieldOfView(90);
        camera.getTransforms().addAll(rotateY, rotateX);

        // create sphere
        PhongMaterial material = new PhongMaterial();
        Sphere sphere = new Sphere(100, 96);
        sphere.setCullFace(CullFace.NONE);
        sphere.setMaterial(material);

        // create scene
        Group group = new Group(camera, sphere, new AmbientLight(Color.WHITE));
        Scene scene = new Scene(group, 800, 600, true, SceneAntialiasing.BALANCED);
        scene.setCamera(camera);

        // mouse events
        scene.setOnMousePressed(event -> {
            oldX = event.getScreenX();
            oldY = event.getScreenY();
        });

        scene.setOnMouseDragged(event -> {
            double newX = event.getScreenX();
            double newY = event.getScreenY();
            double cx = newX - oldX;
            double cy = newY - oldY;
            double angleX = rotateX.getAngle();
            double angleY = rotateY.getAngle();
            if (Math.abs(cx) > 3) {
                angleY = angleY - cx / 10;
                oldX = newX;
            }
            if (Math.abs(cy) > 3) {
                angleX = angleX + cy / 10;
                oldY = newY;
            }
            if (angleX > 90) angleX = 90;
            if (angleX < -90) angleX = -90;
            rotateX.setAngle(angleX);
            rotateY.setAngle(angleY);
        });

        scene.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            } else {
                event.consume();
            }
        });

        scene.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                if (!db.getFiles().isEmpty()) {
                    Image image = loadImage(db.getFiles().get(0));
                    material.setDiffuseMap(image);
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        primaryStage.setTitle("FXPanoViewer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
