package in.himtech.client.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class Main extends Application {

	private Button btnPublish;
	private Button btnConnect;
	private Button btnSubscribe;
	private Button btnReset;
	private TextArea msgArea;
	private TextField txtPublish;
	private TextField txtConnectionUrl;
	private TextField txtKeepAlive;
	private TextField txtRetry;
	private TextField txtPubTopic;
	private TextField txtSubTopic;

	private MqttClient mqttClient;

	private MessageReceiver msgReceiver;

	public static void main(String[] args) {
		launch(args);
	}

	private void makeConnection() {
		msgArea.appendText("Connecting to Broker.....");
		String connectionURL = txtConnectionUrl.getText().trim();
		String strKeepAlive = txtKeepAlive.getText().trim();
		String strRetry = txtRetry.getText().trim();

		msgArea.appendText("\nConnection URL: " + connectionURL);
		msgArea.appendText("\nKeep Alive Time: " + strKeepAlive + " seconds");
		msgArea.appendText("\nRetry Time: " + strRetry + " seconds");

		try {
			mqttClient = new MqttClient(connectionURL, "Himtech123");
			mqttClient.connect();
			msgArea.appendText("\nConnection successfully established with broker.");
			btnConnect.setStyle("-fx-background-color: #00ff00;");
		} catch (MqttException ex) {
			msgArea.appendText("\nUnable to connect to broker :" + ex.getMessage());
			btnConnect.setStyle("-fx-background-color: #ff0000;");
		}
	}

	private void subscribeConsumerTopic() {
		if (mqttClient != null && mqttClient.isConnected()) {
			String strSubsTopic = txtSubTopic.getText().trim();
			try {
				msgReceiver = new MessageReceiver();
				mqttClient.setCallback(msgReceiver);
				mqttClient.subscribe(strSubsTopic);
				msgArea.appendText("\nSuccessfully subscribe the topic :" + strSubsTopic);
			} catch (MqttException ex) {
				msgArea.appendText("\nUnable to subscribe the topic :" + strSubsTopic);
			}
		}
	}
	
	private void resetMqttTool(){
		if (mqttClient != null && mqttClient.isConnected()) {
			try {
				mqttClient.disconnect();
				msgArea.appendText("\nMqtt client is successfully disconnected from server.");
			} catch (MqttException ex) {
				msgArea.appendText("\nGetting exception while disconnection :" + ex.getMessage());
			}
			txtConnectionUrl.setText("");
			txtKeepAlive.setText("");
			txtPublish.setText("");
			txtRetry.setText("");
			txtPubTopic.setText("");
			txtSubTopic.setText("");
			btnConnect.setStyle("-fx-background-color: #ff0000;");
		}
		
	}
	
	private void publishMessage(){
		if (mqttClient != null && mqttClient.isConnected()) {
			String strPubTopic = txtPubTopic.getText().trim();
			try {
				String message = txtPublish.getText().trim();
				MqttMessage mqttMessage = new MqttMessage();
				mqttMessage.setPayload(message.getBytes());
				mqttMessage.setRetained(false);
				mqttMessage.setQos(0);
				mqttClient.publish(strPubTopic, mqttMessage);
				msgArea.appendText("\nSuccessfully send message to topic :" + strPubTopic);
			} catch (MqttException ex) {
				msgArea.appendText("\nUnable to send message to topic :" + strPubTopic);
			}
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Himtech MQTT Client");

		Scene scene = createFxScene();
		primaryStage.setScene(scene);
		primaryStage.setHeight(360);
		primaryStage.setWidth(570);
		primaryStage.show();
		
		primaryStage.setOnCloseRequest(we -> System.exit(0));
	}

	private Scene createFxScene() {
		HBox hBox = new HBox();
		hBox.setPadding(new Insets(7, 5, 7, 5));
		hBox.setSpacing(10);
		hBox.setAlignment(Pos.BOTTOM_RIGHT);
		hBox.setStyle("-fx-background-color: #999999;");

		btnPublish = new Button("Publish");
		btnPublish.setPrefSize(120, 20);
		btnPublish.setOnAction(e -> {publishMessage();});

		txtPublish = new TextField();
		txtPublish.setPromptText("Enter Message to Publish");
		txtPublish.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(txtPublish, Priority.ALWAYS);
		hBox.getChildren().addAll(txtPublish, btnPublish);

		// Creating a GridPane container
		GridPane grid = new GridPane();
		grid.setPadding(new Insets(10, 10, 10, 10));
		grid.setVgap(5);
		grid.setHgap(5);

		// Defining the Name text field
		txtConnectionUrl = new TextField();
		txtConnectionUrl.setPromptText("Enter Connection URL.");
		GridPane.setConstraints(txtConnectionUrl, 0, 0);
		GridPane.setHgrow(txtConnectionUrl, Priority.ALWAYS);
		grid.getChildren().add(txtConnectionUrl);

		// Defining the Last Name text field
		txtKeepAlive = new TextField();
		txtKeepAlive.setPromptText("Keep Alive");
		txtRetry = new TextField();
		txtRetry.setPromptText("Retry");
		HBox hboxTime = new HBox();
		HBox.setHgrow(txtKeepAlive, Priority.ALWAYS);
		HBox.setHgrow(txtRetry, Priority.ALWAYS);
		hboxTime.getChildren().addAll(txtKeepAlive, txtRetry);
		GridPane.setConstraints(hboxTime, 0, 1);
		GridPane.setHgrow(hboxTime, Priority.ALWAYS);
		grid.getChildren().add(hboxTime);

		// Defining the Last Name text field
		txtPubTopic = new TextField();
		txtPubTopic.setPromptText("Producer Topic");
		txtSubTopic = new TextField();
		txtSubTopic.setPromptText("Consumer Topic");
		HBox hboxTopic = new HBox();
		HBox.setHgrow(txtPubTopic, Priority.ALWAYS);
		HBox.setHgrow(txtSubTopic, Priority.ALWAYS);
		hboxTopic.getChildren().addAll(txtPubTopic, txtSubTopic);
		GridPane.setConstraints(hboxTopic, 0, 2);
		grid.getChildren().add(hboxTopic);

		// Defining the Submit button
		btnConnect = new Button("Connect");
		btnConnect.setPrefSize(120, 20);
		btnConnect.setOnAction((ActionEvent e) -> {makeConnection();});
		GridPane.setConstraints(btnConnect, 1, 0);
		grid.getChildren().add(btnConnect);

		// Defining the Clear button
		btnSubscribe = new Button("Subscribe");
		btnSubscribe.setPrefSize(120, 20);
		btnSubscribe.setOnAction(e -> {subscribeConsumerTopic();});
		GridPane.setConstraints(btnSubscribe, 1, 1);
		grid.getChildren().add(btnSubscribe);

		// Defining the Clear button
		btnReset = new Button("Reset");
		btnReset.setPrefSize(120, 20);
		btnReset.setOnAction(e -> {resetMqttTool();});
		GridPane.setConstraints(btnReset, 1, 2);
		grid.getChildren().add(btnReset);

		msgArea = new TextArea();
		msgArea.setEditable(false);
		ScrollPane sp = new ScrollPane(msgArea);
		sp.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
		sp.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
		sp.setFitToHeight(true);
		sp.setFitToWidth(true);

		BorderPane borderPane = new BorderPane(sp);
		borderPane.setTop(grid);
		borderPane.setBottom(hBox);

		Scene scene = new Scene(borderPane);
		return scene;
	}

	class MessageReceiver implements MqttCallback {

		@Override
		public void connectionLost(Throwable arg0) {
			msgArea.appendText("\nDisconnected From Broker.");
			btnConnect.setStyle("-fx-background-color: #ff0000;");

		}

		@Override
		public void deliveryComplete(IMqttDeliveryToken arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void messageArrived(String arg0, MqttMessage mqttMessage) throws Exception {
			msgArea.appendText("\nReceived Message :" + new String(mqttMessage.getPayload()));

		}

	}

}
