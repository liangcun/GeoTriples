package eu.linkedeodata.geotriples.gui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonGroup;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.DialogCloseListener;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.Form;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Window;
import org.d2rq.db.SQLConnection;
import org.d2rq.db.op.TableOp;
import org.d2rq.db.schema.ColumnDef;
import org.d2rq.db.schema.ColumnName;
import org.d2rq.db.schema.TableName;

import eu.linkedeodata.geotriples.GeneralConnection;
import eu.linkedeodata.geotriples.GeneralConnection.ConnectionType;
import eu.linkedeodata.geotriples.shapefile.ShapefileConnection;

public class SqlForm extends Prompt implements Bindable {
	/**
	 * The "Load" button in the database tab
	 */
	@BXML private PushButton loadButton = null;
	/**
	 * Username for connecting to the db
	 */
	@BXML private TextInput username;
	
	/**
	 * Password for connecting to the db
	 */
	@BXML private TextInput password;
	
	/**
	 * Name of the database
	 */
	@BXML private TextInput dbName;
	
	/**
	 * IP Address for connecting to the db
	 */
	@BXML private TextInput ipAddress;
	
	/**
	 * DB backend
	 */
	@BXML private ListButton dbEngine;
	
	/**
	 * A list that contains all the database tables (must connect to the db first)
	 */
	
	private SQLConnection sqlConnection=null;
	private boolean isLoaded=false;
	@Override
	public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
		dbEngine.setSelectedIndex(0);
		/**
		 * What happens when the Load button is pressed
		 */
		loadButton.setAction(new Action() {
			
			@Override
			public void perform(Component source) {
				boolean invalidInput = false;
            	String uname = username.getText();
            	if (uname.isEmpty() ||  uname == null) {
            		invalidInput = true;
            		Alert.alert(MessageType.ERROR, "You did not specify the username for connecting to the database", SqlForm.this.getWindow(), 
            		new DialogCloseListener() {
            			@Override
            			public void dialogClosed(Dialog dialog, boolean modal) {
            				//System.exit(13);
            			}
            		});
            	}
            	
            	String passw = password.getText();
            	if (passw.isEmpty() || passw == null) {
            		invalidInput = true;
            		Alert.alert(MessageType.ERROR, "You did not specify the password for connecting to the database", SqlForm.this.getWindow(),
            		new DialogCloseListener() {
            			@Override
            			public void dialogClosed(Dialog dialog, boolean modal) {
            				//System.exit(13);
            			}
            		});
            	}
            	
            	String engine = (String) dbEngine.getSelectedItem();
            	if (engine.isEmpty() || engine == null) {
            		invalidInput = true;
            		Alert.alert(MessageType.ERROR, "You did not specify the database back end (MonetDB or PostgreSQL)", SqlForm.this.getWindow(),
            		new DialogCloseListener() {
            			@Override
            			public void dialogClosed(Dialog dialog, boolean modal) {
            				//System.exit(13);
            			}
            		});
            	}
            	
            	String dbname = dbName.getText();
            	if (dbname.isEmpty() || dbname == null) {
            		invalidInput = true;
            		Alert.alert(MessageType.ERROR, "You did not specify the name of the database to which you wish to connect", SqlForm.this.getWindow(), 
            		new DialogCloseListener() {
            			@Override
            			public void dialogClosed(Dialog dialog, boolean modal) {
            				//System.exit(13);
            			}
            		});
            	}
            	
            	
            	String ipaddr = ipAddress.getText();
            	if (ipaddr.isEmpty() || ipaddr == null) {
            		invalidInput = true;
            		Alert.alert(MessageType.ERROR, "You did not specify the IP address for connecting to the database", SqlForm.this.getWindow(),
            		new DialogCloseListener() {
            			@Override
            			public void dialogClosed(Dialog dialog, boolean modal) {
            				//System.exit(13);
            			}
            		});
            	}
            	
            	if (invalidInput) {
            		close(false);
            	}
            	
            	String jdbcDriver = null;
            	if (engine.equals("PostgreSQL")) {
            		jdbcDriver = "org.postgresql.Driver";
            	}
            	else if (engine.equals("MonetDB")) {
            		jdbcDriver = "nl.cwi.monetdb.jdbc.MonetDriver";
            	}
            	
            	//connect to the database according to the credentials provided and refresh the list view
            	sqlConnection = new SQLConnection("jdbc:"+engine.toLowerCase()+"://"+ipaddr+"/"+dbname, jdbcDriver, uname, passw);
            	try {
            		sqlConnection.connection();
            		isLoaded=true;
				} catch (Exception e) {
					Alert.alert("Error connecting to database", SqlForm.this.getOwner());
				}
                        System.out.println("SQLForm: Going to close with success");
            	close(true);
			}
		});
    }
	
	public SQLConnection getSqlConnection() {
		return sqlConnection;
	}
	public void setSqlConnection(SQLConnection sqlConnection) {
		this.sqlConnection = sqlConnection;
	}

	public boolean IsLoaded() {
		return isLoaded;
	}
	
}
