/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package assetmanager;

/**
 *
 * @author josh
 */
public class AssetManagerController {
                                    /*
    @FXML private TreeView<Query> queryTree;
    @FXML private Button zoomOut;
    @FXML private Button zoomIn;
    @FXML private TableView<Asset> table;
    @FXML private TextField search;
    @FXML private FlowPane imageView;
    @FXML private AnchorPane anchorPane1;
    
    @FXML private ToggleButton switchTableView;
    @FXML private ToggleButton switchThumbView;
    
    @FXML private Button addAssetButton;
    @FXML private Button addNewList;
    @FXML private Button delete;

    @FXML private MenuItem deleteListMenuItem;
    @FXML private MenuItem addListMenuItem;
    @FXML private MenuButton queryMenu;

    private Image miniIcons;
    private AssetDB db;
    private TreeItem<Query> root;
    
    
    private static final DataFormat ASSETS = new DataFormat("ASSETS");
    private TreeItem<Query> staticLists;
    private LibraryQuery all;

    public void initialize(URL arg0, ResourceBundle arg1) {
        
        db = AssetDB.getInstance();

        // data setup
        
        Query library = new Query("LIBRARY","----");
        library.setSelectable(false);
        TreeItem<Query> libraryItem = new TreeItem<Query>(library);
        libraryItem.setExpanded(true);

        all = new LibraryQuery("Everything","*",0,1);
        final TreeItem<Query> allitem = new TreeItem<Query>(all);
        Query fonts = new Query("Fonts",AssetDB.FONT,6,2);
        Query symbols = new Query("Symbols",AssetDB.SYMBOLSET,4,0);
        Query textures = new Query("Textures",AssetDB.PATTERN,10,0);
        Query gradients = new Query("Gradients","gradient",19,1);
        Query images = new Query("Images","image",19,2);
        Query palettes = new Query("Palettes",AssetDB.PALETTE,4,5);
        final Query staticList = new Query("LISTS","----");
        staticList.setSelectable(false);
        staticLists = new TreeItem<Query>(staticList);
        staticLists.setExpanded(true);

        for(StaticQuery query : db.getStaticLists()) {
            staticLists.getChildren().add(new TreeItem<Query>(query));
        }

        root = new TreeItem<Query>();
        root.setExpanded(true);
        
        libraryItem.getChildren().addAll(
                allitem,
                new TreeItem<Query>(fonts),
                new TreeItem<Query>(symbols),
                new TreeItem<Query>(textures),
                new TreeItem<Query>(gradients),
                new TreeItem<Query>(images),
                new TreeItem<Query>(palettes)
        );
        root.getChildren().addAll(libraryItem, staticLists);

        queryTree.setRoot(root);
        queryTree.getSelectionModel().select(0);
        queryTree.setEditable(true);





        //declare context menu
        final ContextMenu assetContextMenu = new ContextMenu();


        MenuItem reveal = new MenuItem("Show on desktop");
        reveal.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent actionEvent) {
            }
        });

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(new DeleteAction());

        MenuItem info = new MenuItem("Info");
        info.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent actionEvent) {
            }
        });

        assetContextMenu.getItems().addAll(info, reveal, deleteItem);


        // visuals
        miniIcons = new Image("AssetManager/src/assetmanager/glyphicons-black.png");
        queryTree.setCellFactory(new Callback<TreeView<Query>, TreeCell<Query>>() {
            public TreeCell<Query> call(TreeView<Query> arg0) {
                return new EditableTreeCell();
            }
        });
        
        
        
        zoomOut.setGraphic(getIcon(14, 0));
        zoomIn.setGraphic(getIcon(15, 0));
        
        
        
        switchTableView.setGraphic(getIcon(10,0));
        switchThumbView.setGraphic(getIcon(11,0));

        queryMenu.setGraphic(getIcon(18,0));
        queryMenu.setText("");
        
        
        ObservableList<Asset> assets = FXCollections.observableArrayList(db.getAllAssets());
        table.setItems(assets);
        
        //name column
        TableColumn<Asset, String> nameColumn = new TableColumn<Asset, String>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<Asset, String>("name"));
        nameColumn.setMinWidth(100);
        nameColumn.setEditable(true);

        nameColumn.setCellFactory(new Callback<TableColumn<Asset, String>, TableCell<Asset, String>>() {
            public TableCell<Asset, String> call(TableColumn<Asset, String> assetStringTableColumn) {
                EditableTableCell cell = new EditableTableCell();
                cell.setOnDragDetected(new EventHandler<MouseEvent>() {
                    public void handle(MouseEvent e) {
                        Dragboard db = table.startDragAndDrop(TransferMode.ANY);
                        Map<DataFormat,Object> map = new HashMap<DataFormat, Object>();
                        map.put(DataFormat.PLAIN_TEXT,"foo");
                        db.setContent(map);
                        e.consume();
                    }
                });
                cell.setContextMenu(assetContextMenu);
                return cell;
            }
        });
        nameColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Asset, String>>() {
            public void handle(TableColumn.CellEditEvent<Asset, String> e) {
                e.getTableView().getItems()
                        .get(e.getTablePosition().getRow())
                        .setName(e.getNewValue());
            }
        });

        table.getColumns().add(nameColumn);
        table.setEditable(true);
        
        
        //kind column
        TableColumn<Asset, String> kindColumn = new TableColumn<Asset, String>("Kind");
        kindColumn.setCellValueFactory(new PropertyValueFactory<Asset, String>("kind"));
        kindColumn.setMinWidth(100);
        kindColumn.setEditable(false);
        table.getColumns().add(kindColumn);


        TableColumn<Asset, Asset> previewColumn = new TableColumn<Asset, Asset>("Preview");
        previewColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Asset, Asset>, ObservableValue<Asset>>() {
            public ObservableValue<Asset> call(TableColumn.CellDataFeatures<Asset, Asset> assetImageViewCellDataFeatures) {
                return new ReadOnlyObjectWrapper<Asset>(assetImageViewCellDataFeatures.getValue());
            }
        });
        previewColumn.setCellFactory(new Callback<TableColumn<Asset, Asset>, TableCell<Asset, Asset>>() {
            public TableCell<Asset, Asset> call(TableColumn<Asset, Asset> assetImageViewTableColumn) {
                return new TableCell<Asset, Asset>() {
                    @Override
                    protected void updateItem(Asset asset, boolean empty) {
                        super.updateItem(asset, empty);
                        if (!empty) {
                            if(asset.getKind().equals(AssetDB.PATTERN)) {
                                setGraphic(RenderUtil.patternToImage(asset));
                                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                                return;
                            }
                            if(asset.getKind().equals(AssetDB.PALETTE)) {
                                setGraphic(new ImageView(RenderUtil.toImage((Palette)asset)));
                                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                                return;
                            }
                            if(asset.getKind().equals(AssetDB.FONT)) {
                                setGraphic(RenderUtil.fontToImage(asset));
                                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                                return;
                            }
                            setText("---");
                        }
                    }
                };
            }
        });
        previewColumn.setMinWidth(110);
        previewColumn.setPrefWidth(110);
        previewColumn.setMaxWidth(200);
        previewColumn.setEditable(false);
        table.getColumns().add(previewColumn);
        
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        
        
        search.promptTextProperty().set("search");





        
        
        //event handlers
        queryTree.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<TreeItem<Query>>() {
            public void changed(ObservableValue<? extends TreeItem<Query>> value, TreeItem<Query> oldItem, TreeItem<Query> newItem) {
                if(value == null) return;
                if(value.getValue() == null) return;
                Query query = value.getValue().getValue();
                if(!query.isSelectable()) return;
                List<Asset> results = query.execute(db);
                table.setItems(FXCollections.observableList(results));
                
                imageView.getChildren().clear();
                
                List<Node> images = new ArrayList<Node>();
                for(Asset a : results) {
                    if(AssetDB.PATTERN.equals(a.getKind())) {
                        try {
                            images.add(new ImageView(a.getFile().toURI().toURL().toExternalForm()));
                        } catch (MalformedURLException ex) {
                            ex.printStackTrace();
                            Logger.getLogger(AssetManagerController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if(AssetDB.FONT.equals(a.getKind())) {
                        images.add(new Label("Font: " + a.getName()));
                    }
                }
                imageView.getChildren().addAll(images);
            }
        });
        search.textProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
                table.setItems(FXCollections.observableList(db.searchByAnyText(arg2)));
            }
        });


        anchorPane1.setVisible(false);
        table.setVisible(true);

        switchTableView.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent arg0) {
                anchorPane1.setVisible(false);
                table.setVisible(true);
            }
        });
        
        switchThumbView.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent arg0) {
                anchorPane1.setVisible(true);
                table.setVisible(false);
            }
        });
        
        
        addAssetButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent arg0) {
                FileChooser fc = FileChooserBuilder.create().title("Add file or directory").build();
                List<File> files = fc.showOpenMultipleDialog(null);
                if(files != null) {
                    try {
                        addFiles(files.toArray(new File[0]));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        Logger.getLogger(AssetManagerController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        });
        
        addNewList.setOnAction(addNewListAction);
        addListMenuItem.setOnAction(addNewListAction);
        
        
        delete.setOnAction(new DeleteAction());

        deleteListMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent actionEvent) {
                TreeItem<Query> item = queryTree.getSelectionModel().getSelectedItem();
                Query currentQuery = queryTree.getSelectionModel().getSelectedItem().getValue();
                if(!(currentQuery instanceof StaticQuery)) return;
                StaticQuery sq = (StaticQuery) currentQuery;
                u.p("deleting the query : " + sq.getName());
                //delete the underlying query
                db.delete(sq);
                //remove from the tree
                item.getParent().getChildren().remove(item);
                queryTree.getSelectionModel().clearSelection();
                queryTree.getSelectionModel().select(allitem);
            }
        });
    }


    private EventHandler<ActionEvent> addNewListAction = new EventHandler<ActionEvent>() {
        public void handle(ActionEvent e) {
            StaticQuery custom = db.createStaticList("new list");
            staticLists.getChildren().add(new TreeItem<Query>(custom));
        }
    };



    private ImageView getIcon(int x, int y) {
        if(x == -1 || y == -1) {
            x = 0; y = 0;
        }
        ImageView bottomAnchorImage = new ImageView(miniIcons);
        bottomAnchorImage.setViewport(new Rectangle2D(x * 24, y * 24, 15, 15));
        return bottomAnchorImage;
    }

    private static void p(String s) {
        System.out.println(s);
    }
    
    
    private void addFiles(File[] files) throws IOException {
        for(File file : files) {
            p("file: " + file);
            if(file.exists()) {
                if(file.isDirectory()) {
                    addFiles(file.listFiles());
                } else {
                    processfile(file);
                }
            }
        }
    }

    private void processfile(File file) throws IOException {
        //assume png is a texture
        if(file.getName().toLowerCase().endsWith(".png")) {
            db.copyAndAddPattern(file);
        }
        if(file.getName().toLowerCase().endsWith(".ttf")) {
            db.copyAndAddFont(file);
        }
    }

    private EventHandler<? super DragEvent> staticQueryEnter = new EventHandler<DragEvent>() {
        public void handle(DragEvent e) {
            TreeCell<Query> cell = (TreeCell<Query>) e.getTarget();
            cell.getStyleClass().add("droptarget");
            e.consume();
        }
    };
    private EventHandler<? super DragEvent> staticQueryOver = new EventHandler<DragEvent>() {
        public void handle(DragEvent e) {
            if(e.getGestureSource() == table) {
                e.acceptTransferModes(TransferMode.ANY);
            }
            e.consume();
        }
    };
    
    private EventHandler<? super DragEvent> staticQueryExit = new EventHandler<DragEvent>() {
        public void handle(DragEvent e) {
            TreeCell<Query> cell = (TreeCell<Query>) e.getTarget();
            cell.getStyleClass().remove("droptarget");
            e.consume();
        }
    };
    
    private EventHandler<? super DragEvent> staticQueryDrop = new EventHandler<DragEvent>() {
        public void handle(DragEvent e) {
            boolean success = false;
            TreeCell<Query> cell = (TreeCell<Query>) e.getGestureTarget();
            Query query = cell.getTreeItem().getValue();
            if(query instanceof StaticQuery && e.getGestureSource() == table) {
                Dragboard db = e.getDragboard();
                ObservableList<TablePosition> cells = table.getSelectionModel().getSelectedCells();
                for(TablePosition tp : cells) {
                    Asset asset = table.getItems().get(tp.getRow());
                    AssetManagerController.this.db.addToStaticList((StaticQuery)query,asset);
                }
                success = true;
            }
            e.setDropCompleted(success);
            e.consume();
        }
    };

    
    
    private EventHandler<? super DragEvent> libraryQueryEnter = new EventHandler<DragEvent>() {
        public void handle(DragEvent e) {
            if(e.getDragboard().hasFiles()) {
                TreeCell<Query> cell = (TreeCell<Query>) e.getTarget();
                cell.setTextFill(Color.RED);
            }
            e.consume();
        }
    };
    
    private EventHandler<? super DragEvent> libraryQueryOver = new EventHandler<DragEvent>() {
        public void handle(DragEvent e) {
            if(e.getDragboard().hasFiles()) {
                e.acceptTransferModes(TransferMode.ANY);
            }
        }
    };
    
    private EventHandler<? super DragEvent> libraryQueryExit = new EventHandler<DragEvent>() {
        public void handle(DragEvent arg0) {
        }
    };
    
    private EventHandler<? super DragEvent> libraryQueryDrop = new EventHandler<DragEvent>() {
        public void handle(DragEvent e) {
            boolean success = false;
            if(e.getDragboard().hasFiles()) {
                for(File file : e.getDragboard().getFiles()) {
                    if(file.getName().endsWith(".png")) {
                        try {
                            db.copyAndAddPattern(file);
                        } catch (IOException ex) {
                            Logger.getLogger(AssetManagerController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                success = true;
            }
            e.setDropCompleted(success);
            e.consume();
        }
    };

    private static class EditableTableCell extends TableCell<Asset, String> {
        private TextField textField;

        private EditableTableCell() {
        }

        @Override
        public void startEdit() {
            super.startEdit();
            if(textField == null) {
                createTextField();
            }
            setGraphic(textField);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            textField.selectAll();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getString());
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);    //To change body of overridden methods use File | Settings | File Templates.
            if(empty) {
                setText(null);
                setGraphic(null);
            } else {
                if(isEditing()) {
                    if(textField != null) {
                        textField.setText(getString());
                    }
                    setGraphic(textField);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                } else {
                    setText(getItem());
                    setContentDisplay(ContentDisplay.TEXT_ONLY);
                }
            }
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setMinWidth(this.getWidth()-this.getGraphicTextGap()*2);
            textField.setOnKeyPressed(new EventHandler<KeyEvent>() {
                public void handle(KeyEvent keyEvent) {
                    if(keyEvent.getCode() == KeyCode.ENTER) {
                        commitEdit(textField.getText());
                    } else if(keyEvent.getCode() == KeyCode.ESCAPE) {
                        cancelEdit();
                    }
                }
            });
        }

        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }

    private class EditableTreeCell extends TreeCell<Query> {
        private TextField textField;
        private EditableTreeCell() {
        }

        @Override
        public void startEdit() {
            if(!(getItem() instanceof StaticQuery)) return;
            super.startEdit();
            u.p("starting to edit");
            if(textField == null) {
                createTextField();
            }
            setGraphic(textField);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            textField.selectAll();
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setMinWidth(this.getWidth()-this.getGraphicTextGap()*2);
            textField.setOnKeyPressed(new EventHandler<KeyEvent>() {
                public void handle(KeyEvent keyEvent) {
                    if(keyEvent.getCode() == KeyCode.ENTER) {
                        getItem().setName(textField.getText());
                        commitEdit(getItem());
                    } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
                        cancelEdit();
                    }
                }
            });
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getString());
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }

        @Override
        protected void updateItem(Query query, boolean empty) {
            super.updateItem(query, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
                return;
            }
            if(isEditing()) {
                if(textField != null) {
                    textField.setText(getString());
                }
                setGraphic(textField);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                return;
            }
            setText(query.getName());
            setGraphic(getIcon(query.x, query.y));
            if (!query.isSelectable()) {
                getStyleClass().add("tree-header");
            }
            if (query instanceof LibraryQuery) {
                setOnDragEntered(libraryQueryEnter);
                setOnDragOver(libraryQueryOver);
                setOnDragExited(libraryQueryExit);
                setOnDragDropped(libraryQueryDrop);
            }
            if (query instanceof StaticQuery) {
                setOnDragEntered(staticQueryEnter);
                setOnDragOver(staticQueryOver);
                setOnDragExited(staticQueryExit);
                setOnDragDropped(staticQueryDrop);
            }
        }

        public String getString() {
            return getItem() == null ? "" : getItem().getName();
        }
    }

    private class DeleteAction implements EventHandler<ActionEvent> {

        public DeleteAction() {
        }

        public void handle(ActionEvent e) {
            Query currentQuery = queryTree.getSelectionModel().getSelectedItem().getValue();
            if(currentQuery == all) {
                ObservableList<Asset> assets = table.getItems();
                for(TablePosition tp : table.getSelectionModel().getSelectedCells()) {
                    Asset asset = assets.get(tp.getRow());
                    db.removeFromLibrary(asset);
                }
                table.getSelectionModel().clearSelection();
                table.setItems(FXCollections.observableList(all.execute(db)));
                return;
            }
            if(!(currentQuery instanceof StaticQuery)) return;
            StaticQuery staticQuery = (StaticQuery) currentQuery;
            ObservableList<Asset> assets = table.getItems();
            List<Asset> toDelete = new ArrayList<Asset>();
            for(TablePosition tp : table.getSelectionModel().getSelectedCells()) {
                toDelete.add(assets.get(tp.getRow()));
            }
            for(Asset item : toDelete) {
                assets.remove(item);
                db.removeFromStaticList(staticQuery,item);
            }
            table.getSelectionModel().clearSelection();
        }
    }
    */
}
