/*
 * This file is generated by jOOQ.
*/
package org.jooq.example.jpa.jooq;


import javax.annotation.Generated;

import org.jooq.Index;
import org.jooq.OrderField;
import org.jooq.example.jpa.jooq.tables.Actor;
import org.jooq.example.jpa.jooq.tables.Film;
import org.jooq.example.jpa.jooq.tables.FilmActor;
import org.jooq.example.jpa.jooq.tables.Language;
import org.jooq.impl.AbstractKeys;


/**
 * A class modelling indexes of tables of the <code>PUBLIC</code> schema.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.10.4"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Indexes {

    // -------------------------------------------------------------------------
    // INDEX definitions
    // -------------------------------------------------------------------------

    public static final Index PRIMARY_KEY_3 = Indexes0.PRIMARY_KEY_3;
    public static final Index FKD2YJC1RU34H1SMWLA3FX7B6NX_INDEX_2 = Indexes0.FKD2YJC1RU34H1SMWLA3FX7B6NX_INDEX_2;
    public static final Index FKN2UB730RPO5B5E9X6U2LWL9FT_INDEX_2 = Indexes0.FKN2UB730RPO5B5E9X6U2LWL9FT_INDEX_2;
    public static final Index PRIMARY_KEY_2 = Indexes0.PRIMARY_KEY_2;
    public static final Index FK43SD2F45W7YN0GAXQ94EHTWT2_INDEX_7 = Indexes0.FK43SD2F45W7YN0GAXQ94EHTWT2_INDEX_7;
    public static final Index PRIMARY_KEY_7 = Indexes0.PRIMARY_KEY_7;
    public static final Index PRIMARY_KEY_C = Indexes0.PRIMARY_KEY_C;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Indexes0 extends AbstractKeys {
        public static Index PRIMARY_KEY_3 = createIndex("PRIMARY_KEY_3", Actor.ACTOR, new OrderField[] { Actor.ACTOR.ACTORID }, true);
        public static Index FKD2YJC1RU34H1SMWLA3FX7B6NX_INDEX_2 = createIndex("FKD2YJC1RU34H1SMWLA3FX7B6NX_INDEX_2", Film.FILM, new OrderField[] { Film.FILM.LANGUAGE_LANGUAGEID }, false);
        public static Index FKN2UB730RPO5B5E9X6U2LWL9FT_INDEX_2 = createIndex("FKN2UB730RPO5B5E9X6U2LWL9FT_INDEX_2", Film.FILM, new OrderField[] { Film.FILM.ORIGINALLANGUAGE_LANGUAGEID }, false);
        public static Index PRIMARY_KEY_2 = createIndex("PRIMARY_KEY_2", Film.FILM, new OrderField[] { Film.FILM.FILMID }, true);
        public static Index FK43SD2F45W7YN0GAXQ94EHTWT2_INDEX_7 = createIndex("FK43SD2F45W7YN0GAXQ94EHTWT2_INDEX_7", FilmActor.FILM_ACTOR, new OrderField[] { FilmActor.FILM_ACTOR.ACTORS_ACTORID }, false);
        public static Index PRIMARY_KEY_7 = createIndex("PRIMARY_KEY_7", FilmActor.FILM_ACTOR, new OrderField[] { FilmActor.FILM_ACTOR.FILMS_FILMID, FilmActor.FILM_ACTOR.ACTORS_ACTORID }, true);
        public static Index PRIMARY_KEY_C = createIndex("PRIMARY_KEY_C", Language.LANGUAGE, new OrderField[] { Language.LANGUAGE.LANGUAGEID }, true);
    }
}
