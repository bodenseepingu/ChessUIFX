/*
 * Copyright (c) 2015, JUG Bodensee
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *    following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and 
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF 
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.jug.bodensee;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.scenicview.ScenicView;

/**
 *
 * @author MICHELB
 */
public class ChessUIFX extends Application {

    private int style = 0;
    private Stage stage;

    private final Map<Integer, String> figures = new HashMap<Integer, String>();

//&#9812; = ♔
//&#9813; = ♕
//&#9814; = ♖
//&#9815; = ♗
//&#9816; = ♘
//&#9817; = ♙
//&#9818; = ♚
//&#9819; = ♛
//&#9820; = ♜
//&#9821; = ♝
//&#9822; = ♞
//&#9823; = ♟
    public ChessUIFX() {
        figures.put(56, "♖");
        figures.put(57, "♘");
        figures.put(58, "♗");
        figures.put(59, "♕");
        figures.put(60, "♔");
        figures.put(61, "♗");
        figures.put(62, "♘");
        figures.put(63, "♖");
        IntStream.range(48, 56).forEach(i -> figures.put(i, "♙"));  //weisser Bauer

        IntStream.range(8, 16).forEach(i -> figures.put(i, "♟")); //schwarzer Bauer
        figures.put(0, "♜");
        figures.put(1, "♞");
        figures.put(2, "♝");
        figures.put(3, "♛");
        figures.put(4, "♚");
        figures.put(5, "♝");
        figures.put(6, "♞");
        figures.put(7, "♜");
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        
        final UCIController uciController = new UCIController("/Users/sven/Downloads/stockfish-6-mac/Mac/stockfish-6-64");        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                uciController.stop();
            }
        });
        stage = primaryStage;
        stage.initStyle(StageStyle.DECORATED);
        double width = 80f;
        double heigth = 80f;
        BorderPane root = new BorderPane();
        Pane chessboardContainer = new Pane();
        chessboardContainer.getStyleClass().add("board");
        root.setCenter(chessboardContainer);
        Button b = new Button("Siggi-Go!)");
        b.setOnAction(a -> {
            uciController.startNewGame();
            uciController.go();
        });
        TextArea textArea = new TextArea();
        textArea.setMinHeight(150);
        textArea.setMinWidth(250);
        uciController.lastLineFromEngineProperty().addListener((ov, old, newValue) -> {
            Platform.runLater(() -> textArea.appendText(newValue + "\n"));
        });
        HBox box = new HBox(textArea, b);
        root.setBottom(box);
        Scene scene = new Scene(root, width * 8, heigth * 8);
        chessboardContainer.getStylesheets().add(this.getClass().getResource("chess.css").toExternalForm());

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Rectangle rectangle = new Rectangle(heigth, width);
                rectangle.setLayoutX(rectangle.getWidth() * x);
                rectangle.setLayoutY(rectangle.getHeight() * y);
                if ((x + 1 * y + 1) % 2 == 0)  {
                    rectangle.getStyleClass().add("black");
                    rectangle.getStyleClass().add("field");
                } else {
                    rectangle.getStyleClass().add("white");
                    rectangle.getStyleClass().add("field");
                }

                rectangle.widthProperty().bind(Bindings.divide(chessboardContainer.widthProperty(), 8d));
                rectangle.heightProperty().bind(Bindings.divide(chessboardContainer.heightProperty(), 8d));
               
                StackPane stackPane = new StackPane(rectangle);
                stackPane.getStyleClass().add("fieldWrapper");
                
                stackPane.layoutXProperty().bind(Bindings.multiply(x, Bindings.divide(chessboardContainer.widthProperty(), 8d)));
                stackPane.layoutYProperty().bind(Bindings.multiply(y, Bindings.divide(chessboardContainer.heightProperty(), 8d)));

                //find Figure
                String figure = figures.get(x + 8 * y);
                if (null != figure) {
                    final Label piece = new Label(figure);
                    piece.getStyleClass().add("piece");

                    piece.setOnMouseClicked(e -> {
                        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(3), piece);
                        rotateTransition.setFromAngle(0d);
                        rotateTransition.setByAngle(360d);
                        rotateTransition.play();
                    });

                    stackPane.getChildren().add(piece);
                    stackPane.rotateProperty().bind(chessboardContainer.rotateProperty().negate());
                }

                chessboardContainer.getChildren().add(stackPane);
            }
        }

        MenuBar bar = new MenuBar();
        Menu menu = new Menu("Actions");
        MenuItem rotateBoard = new MenuItem("Rotate board");
        rotateBoard.setOnAction(e -> chessboardContainer.rotateProperty().set(chessboardContainer.rotateProperty().get()+180));
        
        MenuItem scenicView = new MenuItem("ScenicView");
        scenicView.setOnAction(e -> ScenicView.show(scene));

        menu.getItems().addAll(rotateBoard, scenicView);

        bar.getMenus().add(menu);
        
        bar.setUseSystemMenuBar(false);
        root.setTop(bar);
        primaryStage.setTitle("JUG Bodensee ChessUIFX");
        primaryStage.setScene(scene);
        primaryStage.show();
        uciController.init();        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
