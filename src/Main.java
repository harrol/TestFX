import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPaneBuilder;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.TreeViewBuilder;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.web.WebView;
import javafx.scene.web.WebViewBuilder;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Harro Lissenberg
 */
public class Main extends Application {

    public static final String DEFAULT_URL = "http://www.chess-ix.com";

    public static void main(String[] args) {
        launch(args);
    }

    @Override public void start(Stage stage) throws Exception {
        TreeItem<SimpleNode> root = new TreeItem<SimpleNode>(new SimpleNode("ROOT", ""));
        final TextArea text = new TextArea("<<<select a leaf>>>");
        text.setWrapText(true);
        text.setPrefHeight(400);
        root.setExpanded(true);

        final TreeView tree = TreeViewBuilder.create()
                .showRoot(true)
                .editable(false)
                .build();
        tree.setRoot(root);
        tree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override public void changed(ObservableValue observableValue, Object oldValue, Object newValue) {
                TreeItem<SimpleNode> selected = (TreeItem) newValue;
                if (selected != null) {
                    text.setText(selected.getValue().value);
                }
            }
        });

        final WebView webView = WebViewBuilder.create().build();
        webView.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State oldState, Worker.State newState) {
                if (newState == Worker.State.SUCCEEDED) {
                    buildDOMTree(tree, webView.getEngine().getDocument());
                }
            }
        });
        webView.setPrefHeight(800);


        final TextField url = new TextField(DEFAULT_URL);
        url.setPrefWidth(400);
        Button go = new Button("GO");
        go.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent actionEvent) {
                String newUrl = url.getText();
                try {
                    new URL(newUrl);
                    webView.getEngine().load(newUrl);

                } catch (MalformedURLException e) {
                    url.setText(DEFAULT_URL);
                    webView.getEngine().load(DEFAULT_URL);
                }
            }
        });

        HBox hBox = HBoxBuilder.create()
                .children(url, go)
                .build();


        SplitPane splitPane = SplitPaneBuilder.create()
                .items(tree, webView)
                .dividerPositions(new double[]{0.2})
                .orientation(Orientation.HORIZONTAL)
                .build();

        VBox vBox = VBoxBuilder.create()
                .children(hBox, splitPane)
                .build();
        SplitPane vertSplit = SplitPaneBuilder.create()
                .items(vBox, text)
                .dividerPositions(new double[]{0.8})
                .orientation(Orientation.VERTICAL)
                .build();
        Scene scene = SceneBuilder.create()
                .width(1200)
                .height(900)
                .root(vertSplit)
                .build();

        stage.setScene(scene);
        stage.setTitle("TestFX");
        stage.show();


    }

    private void buildDOMTree(TreeView tree, Document document) {
        addChildren(tree.getRoot(), document.getChildNodes());
    }

    private void addChildren(TreeItem<SimpleNode> treeItem, NodeList nodes) {
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                Node item = nodes.item(i);
                String value = item.getTextContent() != null ? item.getTextContent() : item.getNodeValue();
                if (item.hasChildNodes()) {
                    TreeItem<SimpleNode> child = new TreeItem(new SimpleNode(item.getNodeName(), value));
                    treeItem.getChildren().add(child);
                    addChildren(child, item.getChildNodes());
                } else {
                    treeItem.getChildren().add(new TreeItem(new SimpleNode(item.getNodeName(), value)));
                }
            }
        }
    }

    static class SimpleNode {
        String description;
        String value;

        SimpleNode(String description, String value) {
            this.description = description;
            if (value != null) {
                this.value = value.trim();
            }
        }

        @Override public String toString() {
            return description;
        }
    }


}
