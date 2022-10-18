/*
 * This file is generated by jOOQ.
 */
package djma.db.jooq.tables;


import djma.db.jooq.Keys;
import djma.db.jooq.Public;
import djma.db.jooq.tables.records.ContactRecord;

import java.util.function.Function;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Function4;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Records;
import org.jooq.Row4;
import org.jooq.Schema;
import org.jooq.SelectField;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Contact extends TableImpl<ContactRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.contact</code>
     */
    public static final Contact CONTACT = new Contact();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ContactRecord> getRecordType() {
        return ContactRecord.class;
    }

    /**
     * The column <code>public.contact.resourceName</code>.
     */
    public final TableField<ContactRecord, String> RESOURCENAME = createField(DSL.name("resourceName"), SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>public.contact.name</code>.
     */
    public final TableField<ContactRecord, String> NAME = createField(DSL.name("name"), SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.contact.email</code>.
     */
    public final TableField<ContactRecord, String> EMAIL = createField(DSL.name("email"), SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.contact.phone</code>.
     */
    public final TableField<ContactRecord, String> PHONE = createField(DSL.name("phone"), SQLDataType.VARCHAR(255), this, "");

    private Contact(Name alias, Table<ContactRecord> aliased) {
        this(alias, aliased, null);
    }

    private Contact(Name alias, Table<ContactRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.contact</code> table reference
     */
    public Contact(String alias) {
        this(DSL.name(alias), CONTACT);
    }

    /**
     * Create an aliased <code>public.contact</code> table reference
     */
    public Contact(Name alias) {
        this(alias, CONTACT);
    }

    /**
     * Create a <code>public.contact</code> table reference
     */
    public Contact() {
        this(DSL.name("contact"), null);
    }

    public <O extends Record> Contact(Table<O> child, ForeignKey<O, ContactRecord> key) {
        super(child, key, CONTACT);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public UniqueKey<ContactRecord> getPrimaryKey() {
        return Keys.CONTACT_PKEY;
    }

    @Override
    public Contact as(String alias) {
        return new Contact(DSL.name(alias), this);
    }

    @Override
    public Contact as(Name alias) {
        return new Contact(alias, this);
    }

    @Override
    public Contact as(Table<?> alias) {
        return new Contact(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public Contact rename(String name) {
        return new Contact(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Contact rename(Name name) {
        return new Contact(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public Contact rename(Table<?> name) {
        return new Contact(name.getQualifiedName(), null);
    }

    // -------------------------------------------------------------------------
    // Row4 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row4<String, String, String, String> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    public <U> SelectField<U> mapping(Function4<? super String, ? super String, ? super String, ? super String, ? extends U> from) {
        return convertFrom(Records.mapping(from));
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    public <U> SelectField<U> mapping(Class<U> toType, Function4<? super String, ? super String, ? super String, ? super String, ? extends U> from) {
        return convertFrom(toType, Records.mapping(from));
    }
}
