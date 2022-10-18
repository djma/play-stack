/*
 * This file is generated by jOOQ.
 */
package djma.db.jooq;


import djma.db.jooq.tables.Contact;
import djma.db.jooq.tables.Kvstore;
import djma.db.jooq.tables.records.ContactRecord;
import djma.db.jooq.tables.records.KvstoreRecord;

import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables in
 * public.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<ContactRecord> CONTACT_PKEY = Internal.createUniqueKey(Contact.CONTACT, DSL.name("contact_pkey"), new TableField[] { Contact.CONTACT.RESOURCENAME }, true);
    public static final UniqueKey<KvstoreRecord> KVSTORE_PKEY = Internal.createUniqueKey(Kvstore.KVSTORE, DSL.name("kvstore_pkey"), new TableField[] { Kvstore.KVSTORE.KEY }, true);
}
