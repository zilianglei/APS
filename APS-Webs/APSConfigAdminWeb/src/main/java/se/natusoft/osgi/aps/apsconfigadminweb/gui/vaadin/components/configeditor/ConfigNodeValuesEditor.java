/*
 *
 * PROJECT
 *     Name
 *         APS Configuration Admin Web
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Edits configurations registered with the APSConfigurationService.
 *
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *
 * LICENSE
 *     Apache 2.0 (Open Source)
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 * AUTHORS
 *     Tommy Svensson (tommy.svensson@biltmore.se)
 *         Changes:
 *         2012-04-08: Created!
 *
 */
package se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.components.configeditor;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import se.natusoft.osgi.aps.api.core.configold.model.admin.APSConfigEnvironment;
import se.natusoft.osgi.aps.api.core.configold.model.admin.APSConfigReference;
import se.natusoft.osgi.aps.api.core.configold.model.admin.APSConfigValueEditModel;
import se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.components.configeditor.event.ValueChangedEvent;
import se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.components.configeditor.event.ValueChangedListener;
import se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.css.CSS;
import se.natusoft.osgi.aps.tools.web.vaadin.components.HorizontalLine;

import java.util.List;

/**
 * Edits the values of a configold node.
 */
public class ConfigNodeValuesEditor extends Panel {
    //
    // Private Members
    //

    /** The data source for this component. */
    private DataSource dataSource = null;

    //----- GUI Components -----//

    /** The root layout of the component. */
    private VerticalLayout rootLayout = null;

    /** This event is sent by all *Value component. */
    private ValueChangedListener valueChangedListener = new ValueChangedListener() {
        @Override
        public void valueChanged(ValueChangedEvent event) {
            valueComponentChanged(event.getValueReference(), event.getValue());
        }
    };

    //
    // External Requirements APIs.
    //

    /**
     * Provides data for this component.
     */
    public static interface DataSource extends ValueComponentListEditor.DataSource {

        /**
         * Returns the description of the node.
         */
        String getNodeDescription();

        /**
         * Returns true if this is a "many" node, in which case the index is valid.
         */
        public boolean isManyNode();

        /**
         * Returns the values of the current node.
         */
        List<APSConfigReference> getNodeValues();

        /**
         * Returns the indexed state of the node.
         */
        public boolean isIndexed();

        /**
         * Returns the current index if the current node is a "many" type node or -1 otherwise.
         */
        int getIndex();

        /**
         * Returns the current configold environment.
         */
        APSConfigEnvironment getCurrentConfigEnvironment();
    }

    //
    // Constructors
    //

    /**
     * Creates a new ConfigNodeValuesEditor instance.
     */
    public ConfigNodeValuesEditor() {
        setStyleName(CSS.APS_CONFIG_NODE_VALUES_EDITOR);
        this.rootLayout = new VerticalLayout();
        this.rootLayout.setMargin(true);
        this.rootLayout.setSpacing(true);
        setContent(this.rootLayout);
    }

    //
    // Public Methods
    //

    /**
     * Sets a data source to use by this component.
     *
     * @param dataSource The data source to set.
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Loads the values to edit for the specified node.
     */
    public void refreshData() {

        this.rootLayout.removeAllComponents();

        Label nodeDescription = new Label(this.dataSource.getNodeDescription(), ContentMode.HTML);
        this.rootLayout.addComponent(nodeDescription);

        this.rootLayout.addComponent(new HorizontalLine());

        // If we have a node that there can be only one of or if we have a "many" node where an instance of that
        // "many" node is selected then render the configuration values of that node.
        if (!this.dataSource.isManyNode() || (this.dataSource.isIndexed())) {

            List<APSConfigReference> nodeValues = this.dataSource.getNodeValues();

            for (APSConfigReference valueRef : nodeValues) {
                APSConfigValueEditModel valueModel = valueRef.getConfigValueEditModel();

                // Name
                {
                    String nameText = "<b>" + valueModel.getName() + "</b>";
                    if (valueModel.isConfigEnvironmentSpecific()) {
                        nameText += " (" + this.dataSource.getCurrentConfigEnvironment().getName() + ")";
                    }
                    if (valueModel.getDatePattern().trim().length() > 0) {
                        nameText += " [ " + valueModel.getDatePattern().toLowerCase() + " ]";
                    }
                    Label name = new Label(nameText, ContentMode.HTML);
                    this.rootLayout.addComponent(name);
                }

                // Description
                {
                    Label description = new Label(valueModel.getDescription());
                    this.rootLayout.addComponent(description);
                }

                // Value
                {
                    ValueComponent valueComponent;

                    // Date value
                    if (valueModel.getDatePattern() != null && valueModel.getDatePattern().trim().length() > 0) {
                        valueComponent = new DateValue(valueRef);
                    }
                    // Boolean value
                    else if (valueModel.isBoolean()) {
                        valueComponent = new BooleanValue(valueRef);
                    }
                    // Value with set of valid values.
                    else if (valueModel.getValidValues().length > 0) {
                        valueComponent = new ValidValuesValue(valueRef);
                    }
                    // Text value by default
                    else {
                        valueComponent = new TextValue(valueRef);
                    }

                    if (valueModel.isMany()) {
                        ValueComponentListEditor valueComponentListEditor = new ValueComponentListEditor(valueRef, valueComponent);
                        valueComponentListEditor.setWidth("95%");
                        valueComponentListEditor.setDataSource(this.dataSource);
                        valueComponentListEditor.refreshData();
                        this.rootLayout.addComponent(valueComponentListEditor);
                    } else {
                        valueComponent.addListener(this.valueChangedListener);
                        valueComponent.setComponentValue(this.dataSource.getValue(valueRef), false);
                        valueComponent.getComponent().setWidth("95%");
                        this.rootLayout.addComponent(valueComponent.getComponent());
                    }
                }
            }

            if (nodeValues.isEmpty()) {
                Label label = new Label("There are no values to edit for this node!");
                label.setStyleName(CSS.APS_NO_CONFIG_VALUES_LABEL);
                this.rootLayout.addComponent(label);
            }
        }
        // Otherwise the user has selected the root/parent node of a "many" type node, whose instances are rendered as children of
        // that node. A "many" type node cannot be edited without having an instance of it. Therefore we display some help text
        // instead here.
        else {
            Label label = new Label("You have selected a configuration node that there are zero or more of. This node only represents the " +
                                    "type. Select one of the indexed instances of this node shown as children of this node to edit that " +
                                    "specific instance. Or press the [ + ] button to add an instance node. Press the [ - ] button " +
                                    "to delete the selected instance.");
            this.rootLayout.addComponent(label);
        }
    }

    //
    // Private Methods
    //

    /**
     * Handles changes in value.
     *
     * @param valueRef The configold reference representing the configold value to update.
     * @param newValue The new value to update the configold value with.
     */
    private void valueComponentChanged(APSConfigReference valueRef, String newValue) {
        this.dataSource.updateValue(valueRef, newValue);
    }

}
