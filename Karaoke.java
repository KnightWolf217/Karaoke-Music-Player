
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class Karaoke extends Application {

    static String textFile = System.getProperty("user.dir") + File.separator + "sample_song_data.txt"; //path to file
    static HashMap<String, Song> songs = new HashMap();                                //HashMap to store songs
    static VBox playback = new VBox();                                                 //playback vbox
    static TableView libTable = new TableView<>();                                     //song library table to display songs
    static ObservableList<Song> songList = FXCollections.observableArrayList();         //List of songs to be played in the playlist
    static TableView plTable = new TableView<>();                                       //playlist table to display songs in the playlist
    static Button remove = new Button("Remove Selected from Playlist");                 //remove from playlist button
    static VBox videoPlayer = new VBox();                                               //VBox to hold video player contents
    static Rectangle blank = new Rectangle(640, 360);                                   //empty black screen for the video player
    static Media media;
    static MediaView mediaView;
    static MediaPlayer player;
    static Label nowPlaying;

    @Override
    public void start(Stage primaryStage) {

        HBox main = new HBox();                  //main hbox 
        main.setPadding(new Insets(5));          //padding

        VBox vbox1 = new VBox();                // first vbox
        main.getChildren().add(vbox1);          //insert vbox into main hbox 

        playback.setAlignment(Pos.CENTER);        //aligning elements to center
        playback.setPadding(new Insets(5));       //padding
        playback.setSpacing(5);
        VBox playlist = new VBox();                //playlist vbox
        playlist.setAlignment(Pos.CENTER);
        playlist.setPadding(new Insets(5));
        playlist.setSpacing(5);                    //spacing
        VBox library = new VBox();                 //library vbox
        library.setAlignment(Pos.CENTER);
        library.setPadding(new Insets(5));
        library.setSpacing(5);

        HBox space = new HBox();                 //small horizontal space between the boxes
        space.setPadding(new Insets(3));
        main.getChildren().add(space);
        main.getChildren().add(library);            //inserting library vbox into main hbox

        vbox1.getChildren().add(playback);
        VBox vspace = new VBox();                //small vertical space between playback and playlist box
        vspace.setPadding(new Insets(2));
        vbox1.getChildren().add(vspace);
        vbox1.getChildren().add(playlist);

        String boxstyle = "-fx-border-color: grey;\n" //css
                + "-fx-border-radius: 0px;\n"
                + "-fx-border-insets: 2;\n"
                + "-fx-border-width: 1;\n";

        playback.setStyle(boxstyle);               //applying css
        playlist.setStyle(boxstyle);
        library.setStyle(boxstyle);

        Label heading1 = new Label("Playback");               //labels
        playback.getChildren().add(heading1);

        Label heading2 = new Label("Playlist");
        playlist.getChildren().add(heading2);

        Label heading3 = new Label("Song Library");
        library.getChildren().add(heading3);

        //making the fonts bold
        heading1.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        heading2.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        heading3.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        //creating a blank screen
        playback.getChildren().add(videoPlayer);
        videoPlayer.getChildren().add(blank);

        //control buttons hbox
        HBox controls = new HBox();
        controls.setAlignment(Pos.CENTER);
        playback.getChildren().add(controls);
        controls.setPadding(new Insets(5));
        controls.setSpacing(10);

        //play button
        Button play = new Button("Play");
        play.setPadding(new Insets(5));
        play.setOnAction(value -> {
            //start playing method only if there is no content (when the screen is blank)
            if (videoPlayer.getChildren().contains(blank)) {
                playSongs(songList);
                //else if media content exists then resume the player
            } else if (videoPlayer.getChildren().contains(mediaView)) {
                player.play();
            }
        });

        //pause button
        Button pause = new Button("Pause");
        pause.setPadding(new Insets(5));
        pause.setOnAction(value -> {
            //pause video only if a video is loaded (to prevent errors and exceptions)
            if (videoPlayer.getChildren().contains(mediaView)) {
                player.pause();
            }
        });

        //stop button to stop video player and clear playlist
        Button stop = new Button("Stop/Clear");
        stop.setPadding(new Insets(5));
        stop.setOnAction(value -> {
            if (videoPlayer.getChildren().contains(mediaView)) {
                player.stop();
            }
                videoPlayer.getChildren().clear();
                videoPlayer.getChildren().add(blank);
                playback.getChildren().remove(nowPlaying);        //removing the now playing label
                plTable.getItems().clear();                       //clearing the playlist table
                songList.clear();                                //clearing the playlist media
        });

        //skip track button
        Button skip = new Button("Skip Track");
        skip.setPadding(new Insets(5));
        skip.setOnAction(value -> {
            if (songList.size() != 0) {
                player.stop();
                plTable.getSelectionModel().selectFirst();
                plTable.getItems().remove(plTable.getSelectionModel().selectedItemProperty().getValue());        //removing the song from the table view
                songList.remove(0);                    //removing song from the playlist
                videoPlayer.getChildren().clear();
                videoPlayer.getChildren().add(blank);
                playback.getChildren().remove(nowPlaying);        //removing the now playing label
                playSongs(songList);                        //restart the playSongs method to play the next song in the list
            }
        });

        controls.getChildren().addAll(play, pause, stop, skip);     //displaying the buttons 

        //library box
        HBox searchbox = new HBox();
        searchbox.setSpacing(5);
        library.getChildren().add(searchbox);
        TextField searchBar = new TextField();                //search textfield
        searchBar.setPromptText("Search Songs by Title");     
        searchBar.setPrefWidth(528);
        searchbox.getChildren().add(searchBar);
        Button searchbt = new Button("Search");
        searchbt.setPadding(new Insets(5));
        searchbt.setOnAction((ActionEvent e) -> {
            String criteria = searchBar.getText().toLowerCase().trim(); //search criteria
            //if criteria is blank, reset the table to show all the songs, else search the written criteria
            if ("".equals(criteria)) {
                reset();
            } else {
                search(criteria);
            }
        });
        
        Button resetbt = new Button("Reset");
        resetbt.setPadding(new Insets(5));
        resetbt.setOnAction((ActionEvent e) -> {
            searchBar.clear();                                 //clearing the searchbar
            searchBar.setPromptText("Search Songs by Title");  
            reset();   //reset method
            });
        
        searchbox.getChildren().addAll(searchbt, resetbt);

        //assinging the height of the library table to the height of the stage
        libTable.prefHeightProperty().bind(primaryStage.heightProperty());
        TableColumn title = new TableColumn("Title");
        title.setMinWidth(250);
        title.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn artist = new TableColumn("Artist");
        artist.setMinWidth(180);
        artist.setCellValueFactory(new PropertyValueFactory<>("artist"));

        TableColumn duration = new TableColumn("Duration (s)");
        duration.setMinWidth(100);
        duration.setCellValueFactory(new PropertyValueFactory<>("duration"));

        TableColumn filepath = new TableColumn("File");
        filepath.setMinWidth(99);
        filepath.setCellValueFactory(new PropertyValueFactory<>("file"));

        //creating the table
        libTable.getColumns().addAll(title, artist, duration, filepath);

        //adding the songs to the table using the HashMap
        for (String key : songs.keySet()) {
            libTable.getItems().add(songs.get(key));
        }

        //displaying the table in the song library VBox
        library.getChildren().add(libTable);

        Button addtopl = new Button("Add Selected Song to Playlist");
        addtopl.setPadding(new Insets(5));
        addtopl.setOnAction(value -> {
            //adding the selected song to the playlist if a song is selected on clicking the button
            Object x = libTable.getSelectionModel().selectedItemProperty().getValue();
            if (x != null) {
                plTable.getItems().add(x);      //adding song to table
                songList.add((Song) x);         //adding song to the song list
            }
        });

        library.getChildren().add(addtopl);

        //playlist box
        TableColumn pltitle = new TableColumn("Title");
        pltitle.setSortable(false);                                         
        pltitle.setMinWidth(250);
        pltitle.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn plartist = new TableColumn("Artist");
        plartist.setSortable(false);
        plartist.setMinWidth(250);
        plartist.setCellValueFactory(new PropertyValueFactory<>("artist"));

        TableColumn plduration = new TableColumn("Duration (s)");
        plduration.setSortable(false);
        plduration.setMinWidth(120);
        plduration.setCellValueFactory(new PropertyValueFactory<>("duration"));

        plTable.getColumns().addAll(pltitle, plartist, plduration);
        playlist.getChildren().add(plTable);                         //adding the playlist tableview to the playlist VBox

        //remove from playlist button
        remove.setPadding(new Insets(5));
        remove.setOnAction(value -> {
            Object selectedObj = plTable.getSelectionModel().selectedItemProperty().getValue();     //getting the selected value
            plTable.getItems().remove(selectedObj);      //removing the selected song from the playlist view
            Song toRemove = (Song) selectedObj;         //determining the song object for the selected row in the table
            songList.remove(toRemove);                  //removing the selected song from the songList
        });
        playlist.getChildren().add(remove);

        //creating scene with its dimensions
        Scene scene = new Scene(main, 1320, 800);

        primaryStage.setTitle("Karaoke");    //stage title
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();    //centering the stage on the screen
        primaryStage.show();              //showing the stage
    }

    public static void main(String[] args) {
        
        if (args.length > 0){
            textFile = System.getProperty("user.dir") + File.separator + args[0];       //setting the data textfile to the entered command line argument
        }

        String line;
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(textFile));

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\t");         //seperating each line into individual parts seperated by tab
                String key = parts[0].toLowerCase();        //title of the songs is the key for the hashmap
                String title = parts[0];
                String artist = parts[1];
                String duration = parts[2];
                String filename = parts[3];

                songs.put(key, new Song(title, artist, duration, filename));       //adding the key and song object to the HashMap

            }
            reader.close();
        } catch (FileNotFoundException ex) {                             //catching exceptions
            System.out.println("File not found exception");
        } catch (IOException ex) {
            System.out.println("IO Error");
        }

        launch(args);            //starting the gui
    }

    //search method
    public static void search(String input) {

        libTable.getItems().clear();                //first clearing the table display
        libTable.getItems().add(songs.get(input));  //getting the song from the entered key and displaying the song in the library

    }

    //reset search method
    public static void reset() {
        libTable.getItems().clear();
        for (String key : songs.keySet()) {            //redisplaying all the songs if the search criteria is empty or reset button is pressed
            libTable.getItems().add(songs.get(key));
        }
    }

    public static void playSongs(ObservableList<Song> songList) {
        if (!songList.isEmpty()) {

            File file = new File(songList.get(0).getFile());                 //getting the file for the first song in the playlist
            player = new MediaPlayer(new Media(file.toURI().toString()));    //creating media for that song
            mediaView = new MediaView(player);                     //creating mediaView
            videoPlayer.getChildren().clear();                               //clearing the empty black screen
            videoPlayer.getChildren().add(mediaView);                        //adding the mediaView to show the video
            mediaView.setFitWidth(640);                                      //video dimensions
            mediaView.setFitHeight(360);
            player.play();                                //video player starts
            plTable.getSelectionModel().selectFirst();                                          //selecting the first song in the playlist to play in order
            Song s = (Song) plTable.getSelectionModel().selectedItemProperty().getValue();     //getting the song object from the selected playlist row
            nowPlaying = new Label("Now Playing - " + s.getTitle());                      //label to display the currently playing song
            playback.getChildren().add(nowPlaying);                       //displaying the label

            //at the end of each video remove the video in order from the playlist and play the next video
            player.setOnEndOfMedia(new Runnable() {
                @Override
                public void run() {
                    songList.remove(0);                          //removing the song that just played
                    plTable.getSelectionModel().selectFirst();
                    plTable.getItems().remove(plTable.getSelectionModel().selectedItemProperty().getValue());
                    playback.getChildren().remove(nowPlaying);          //removing the old now playing label
                    videoPlayer.getChildren().clear();
                    videoPlayer.getChildren().add(blank);               //showing the blank black screen
                    playSongs(songList);                      //repeating the method for the remaining songs in the playlist
                }
            });
        }

    }

}
