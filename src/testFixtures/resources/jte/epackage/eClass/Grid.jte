@import net.gradleutil.conf.template.*
@import net.gradleutil.conf.template.EClass
@import net.gradleutil.conf.transform.TransformOptions
@param EClass eClass
@param EPackage ePackage
@param TransformOptions options

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.renderer.TemplateRenderer;

import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Grid of products, handling the visual presentation and filtering of a set of
 * items. This version uses an in-memory data source that is suitable for small
 * data sets.
 */
class ${eClass.name}Grid extends Grid<${eClass.name}> {

    public BeanGrid() {

        setSizeFull();

        @for (EStructuralFeature prop : eClass.getEStructuralFeatures())
            !{String propCap = prop.name.substring(0, 1).toUpperCase() + prop.name.substring(1); }
            !{String optional = (prop.lowerBound == 0) ? "@Optional" : ""; }

            addColumn({ ${eClass.name} cmq -> cmq.${prop.name} }).setHeader("${prop.name}")
                .setFlexGrow(20).setSortable(true).setKey("${prop.name}");
        @endfor

        // If the browser window size changes, check if all columns fit on
        // screen
        // (e.g. switching from portrait to landscape mode)
        UI.getCurrent().getPage().addBrowserWindowResizeListener({ e ->
            setColumnVisibility(e.getWidth())
        });
    }

    private void setColumnVisibility(int width) {
        @for (EStructuralFeature prop : eClass.getEStructuralFeatures())
        getColumnByKey("${prop.name}").setVisible(true);
        @endfor
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // fetch browser width
        UI.getCurrent().getInternals().setExtendedClientDetails(null);
        UI.getCurrent().getPage().retrieveExtendedClientDetails({e ->
            setColumnVisibility(e.getBodyClientWidth());
        });
    }

    public ${eClass.name} getSelectedRow() {
        return asSingleSelect().getValue();
    }

    public void refresh(${eClass.name} product) {
        getDataCommunicator().refresh(product);
    }


}
