/**
 *
 * Copyright 2009-2014 Jayway Products AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.context.administration.surface.accesspoints;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.txt.UniversalEncodingDetector;
import org.qi4j.api.common.Optional;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.Requires;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.administration.form.FieldDefinitionAdminValue;
import se.streamsource.streamflow.api.administration.form.OpenSelectionFieldValue;
import se.streamsource.streamflow.api.administration.form.SelectionFieldValue;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.web.context.administration.forms.definition.FirstFieldInFirstPage;
import se.streamsource.streamflow.web.domain.structure.form.Field;
import se.streamsource.streamflow.web.domain.structure.form.FieldValueDefinition;
import se.streamsource.streamflow.web.domain.structure.form.Fields;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoint;
import se.streamsource.streamflow.web.domain.structure.organization.WebAPReplacedSelectionFieldValues;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Mixins(ReplacementSelectionFieldContext.Mixin.class)
public interface ReplacementSelectionFieldContext
    extends IndexContext<FieldDefinitionAdminValue>,Context
{

    public void addselectionelement( @Name("selection") String name );

    public void removeselectionelement( @Name("index") int index );

    public void moveselectionelement( @Name("name") String name, @Name("index") int index);

    public void changeselectionelementname( @Name("name") String name, @Name("index") int index );

    public void removeallselectionelements();

    public void importvalues( Representation representation );

     abstract class Mixin
        implements ReplacementSelectionFieldContext
    {
        @Structure
        Module module;

        public FieldDefinitionAdminValue index() {
            WebAPReplacedSelectionFieldValues.Data values = RoleMap.role( WebAPReplacedSelectionFieldValues.Data.class );
            Field field = RoleMap.role( Field.class );
            ValueBuilder<FieldDefinitionAdminValue> builder = module.valueBuilderFactory().newValueBuilder(FieldDefinitionAdminValue.class);
            builder.prototype().field().set(EntityReference.getEntityReference( field ));
            SelectionFieldValue fieldValue = values.replacements().get().get( field.toString() );
            if(fieldValue == null )
            {
                fieldValue = module.valueBuilderFactory().newValue( SelectionFieldValue.class );
            }
            builder.prototype().fieldValue().set( fieldValue );
            builder.prototype().description().set("");
            builder.prototype().fieldId().set("");
            builder.prototype().note().set("");
            return builder.newInstance();
        }

        public void addselectionelement( String name )
        {
            WebAPReplacedSelectionFieldValues replacements = RoleMap.role( WebAPReplacedSelectionFieldValues.class );
            Field field = RoleMap.role(Field.class);

            SelectionFieldValue value = replacements.getReplacementFieldValue( field.toString() );

            ValueBuilder<SelectionFieldValue> builder = value != null ? value.<SelectionFieldValue>buildWith() : module.valueBuilderFactory().newValueBuilder(SelectionFieldValue.class);
            builder.prototype().values().get().add( name );
            replacements.changeReplacementFieldValue(field.toString(), builder.newInstance());
        }

        public void removeselectionelement( int index )
        {
            WebAPReplacedSelectionFieldValues replacements = RoleMap.role( WebAPReplacedSelectionFieldValues.class );
            Field field = RoleMap.role( Field.class );

            SelectionFieldValue value = replacements.getReplacementFieldValue(field.toString());

            ValueBuilder<SelectionFieldValue> builder = value != null ? value.<SelectionFieldValue>buildWith() : module.valueBuilderFactory().newValueBuilder(SelectionFieldValue.class);
            if (builder.prototype().values().get().size() > index)
            {
                builder.prototype().values().get().remove( index );
                replacements.changeReplacementFieldValue( field.toString(), builder.newInstance() );
            }
        }

        public void removeallselectionelements()
        {
            WebAPReplacedSelectionFieldValues replacements = RoleMap.role( WebAPReplacedSelectionFieldValues.class );
            Field field = RoleMap.role( Field.class );

            SelectionFieldValue value = replacements.getReplacementFieldValue(field.toString());

            ValueBuilder<SelectionFieldValue> builder = value != null ? value.<SelectionFieldValue>buildWith() : module.valueBuilderFactory().newValueBuilder(SelectionFieldValue.class);

            builder.prototype().values().get().clear();
            replacements.changeReplacementFieldValue( field.toString(), builder.newInstance() );

        }

        public void moveselectionelement( String name, int index )
        {
            WebAPReplacedSelectionFieldValues replacements = RoleMap.role( WebAPReplacedSelectionFieldValues.class );
            Field field = RoleMap.role( Field.class );

            SelectionFieldValue value = replacements.getReplacementFieldValue(field.toString());

            ValueBuilder<SelectionFieldValue> builder = value != null ? value.<SelectionFieldValue>buildWith() : module.valueBuilderFactory().newValueBuilder(SelectionFieldValue.class);
            String element = builder.prototype().values().get().remove( index );
            if ("up".equals( name ))
            {
                builder.prototype().values().get().add( index - 1, element );
            } else
            {
                builder.prototype().values().get().add( index + 1, element );
            }
            replacements.changeReplacementFieldValue( field.toString(), builder.newInstance() );
        }

        public void changeselectionelementname( String name, int index )
        {
            WebAPReplacedSelectionFieldValues replacements = RoleMap.role( WebAPReplacedSelectionFieldValues.class );
            Field field = RoleMap.role( Field.class );

            SelectionFieldValue value = replacements.getReplacementFieldValue(field.toString());

            ValueBuilder<SelectionFieldValue> builder = value != null ? value.<SelectionFieldValue>buildWith() : module.valueBuilderFactory().newValueBuilder(SelectionFieldValue.class);
            builder.prototype().values().get().set( index, name );

            replacements.changeReplacementFieldValue(field.toString(), builder.newInstance());
        }

        public void importvalues(Representation representation)
        {
            boolean hasChanged = false;

            WebAPReplacedSelectionFieldValues replacements = RoleMap.role( WebAPReplacedSelectionFieldValues.class );
            Field field = RoleMap.role( Field.class );

            SelectionFieldValue value = replacements.getReplacementFieldValue(field.toString());

            ValueBuilder<SelectionFieldValue> builder = value != null ? value.<SelectionFieldValue>buildWith() : module.valueBuilderFactory().newValueBuilder(SelectionFieldValue.class);

            try
            {
                List<String> values = new ArrayList<String>();

                if (representation.getMediaType().equals( MediaType.APPLICATION_EXCEL ))
                {
                    HSSFWorkbook workbook = new HSSFWorkbook( representation.getStream() );

                    //extract a user list
                    Sheet sheet1 = workbook.getSheetAt( 0 );
                    for (Row row : sheet1)
                    {
                        values.add( row.getCell( 0 ).getStringCellValue() );
                    }

                } else if (representation.getMediaType().equals( MediaType.TEXT_CSV ))
                {
                    UniversalEncodingDetector encodingDetector = new UniversalEncodingDetector();
                    BufferedInputStream input = new BufferedInputStream( representation.getStream() );
                    Charset detect = encodingDetector.detect( input, new Metadata() );
                    BufferedReader bufReader = new BufferedReader( new InputStreamReader( input, detect.name() ) );
                    String line;
                    while ((line = bufReader.readLine()) != null)
                    {
                        if( !Strings.empty(line))
                            values.add( line );
                    }
                } else
                {
                    throw new ResourceException( Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE );
                }

                for( String string : values )
                {
                    if( !builder.prototype().values().get().contains( string ) )
                    {
                        builder.prototype().values().get().add( string );
                        hasChanged = true;
                    }
                }

                if( hasChanged )
                {
                    replacements.changeReplacementFieldValue(field.toString(), builder.newInstance());
                }

            } catch (IOException ioe)
            {
                throw new ResourceException( Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY );
            }
        }


    }
}
