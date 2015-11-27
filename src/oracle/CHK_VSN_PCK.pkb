-- N:\Lveekhout\CHK_VSN_PCK.pkb

CREATE OR REPLACE package body ALG.chk_vsn_pck is

  g_job_name          varchar2(60) := 'CHK_OBJ_VSN_';
g_revision constant varchar2(30) := 'CHK 1.00.001';


/* Return package revision label */
function revision
 return varchar2
 is
/* GEN REVISION
   Doel    : Versienummer opgeven
   Auteur  : R. Schepers (commentaar A. Wilten)
   Datum   : 10-01-2003

   NB.  Voor aanpassen versienummer van een package
        in de "program data" van de package de
        constante "g_revision" aanpassen
*/
begin
  return g_revision;
end;

function  get_package_revision(p_owner varchar,p_naam varchar,p_type varchar)
return varchar2 is
  l_statement varchar2(200);
  l_revision  varchar2(20);
begin
  if p_type in ('PACKAGE','PACKAGE BODY') then
    begin
      l_statement := 'BEGIN :b1 := '|| p_owner||'.'||p_naam||'.REVISION; END;';
      execute immediate l_statement using out l_revision;
    exception
        when others then
          l_revision := null;
    end;
  else
    l_revision := null;
  end if;
  return l_revision;
end;

/* zetten van de startdatum bij uitvoering raportage */
/* Vastleggen van de objectversies in de tabel */
procedure vastleggen_objectversies
 is
  cursor c_mut(b_periode_van in date) is
  select m.owner,
   m.object_name,
   m.object_type,
   m.status,
   m.created,
   m.last_ddl_time,
   substr(m.comments,1,200) comments,
   m.package_revision,
   v.owner v_owner,
   v.versie,
   'MUT' bron
   from  (SELECT ao.owner,
          ao.object_name,
          AO.OBJECT_TYPE,
          ao.status,
          ao.created,
          ao.last_ddl_time,
          atc.comments,
          chk_vsn_pck.get_package_revision (ao.owner, object_name, ao.object_type)
             package_revision
     FROM    all_objects ao
          LEFT OUTER JOIN
             all_TAB_COMMENTS atc
          ON     atc.owner = ao.owner
             AND atc.table_name = ao.object_name
             AND ao.object_type IN ('TABLE', 'VIEW')
    WHERE ao.object_type IN ('PACKAGE', 'PACKAGE BODY', 'TABLE', 'VIEW')) m
   left outer join chk_objectversies v
     on    v.owner = m.owner
     and   v.object_name = m.object_name
     and   v.object_type = m.object_type
     and   v.einddatum is null
   where m.last_ddl_time> b_periode_van
   and   m.owner = user
   and  not(V.VERSIE is null and m.status = 'INVALID')
   union
 select v.owner,
   v.object_name,
   v.object_type,
   null,
   v.created,
   v.last_ddl_time,
   null,
   null,
   v.owner v_owner,
   v.versie,
   'VER'
   from chk_objectversies v
   where v.einddatum is null
   and v.owner = user
   and not exists
   (select 1 from all_objects o
     where v.owner = o.owner
     and   v.object_name = o.object_name
     and   v.object_type = o.object_type
     );


  l_functie         varchar2(50) := 'chk_vsn_pck.vastleggen_objectversies';
  l_periode_van     date;
  l_aantal_tab_mut  number := 0;
  l_aantal_vw_mut   number := 0;
  l_aantal_pck_mut  number := 0;
  l_rundatum        date   := sysdate;
  l_global_name     varchar2(100);
  l_regeleinde      varchar2(10) := chr(10)||chr(13);
  l_fouten_clob     clob;
  l_fouten_aanwezig boolean := false;
  l_is_aangepast    boolean;
begin
  --
  dbms_lob.createtemporary(l_fouten_clob, true);
  dbms_lob.append(l_fouten_clob,'Bij het controleren van de versies van objecten zijn de volgende problemen geconstateerd: '||l_regeleinde);

  gen_utl_pck.log_msg(p_melding => l_functie||' Begin '||user );
--  select nvl(max(rundatum),to_date('01-01-1900','DD-MM-YYYY'))
  select max(rundatum)
  into l_periode_van
  from chk_vsn_runs
  where owner = user;

  if l_periode_van is null then
    gen_utl_pck.log_msg(p_melding => l_functie||'   Dit is de eerste keer. Zoeken vanaf 1980' );
    l_periode_van := to_date('01-01-1980','DD-MM-YYYY');
  end if;

  gen_utl_pck.log_msg(p_melding => l_functie||'   zoeken vanaf '||to_char(l_periode_van,'DD-MM-YYYY HH24:MI:SS'));

  for r_mut in c_mut(l_periode_van) loop
    gen_utl_pck.log_msg('  gevonden rij : '|| r_mut.bron|| ' '||r_mut.object_type|| ' '|| r_mut.owner|| ' '|| r_mut.object_name || ' versie oud ['|| r_mut.versie || '] nieuw ['|| nvl(r_mut.comments,r_mut.package_revision) || ']');
    /* het record in de versietabel beeindigen indien
       - het object verwijderd is (bron = MUT)
       of
       - het versienummer is aangepast
     */
    l_is_aangepast := (r_mut.bron = 'MUT') and (r_mut.v_owner is not null);

    if (r_mut.bron = 'VER')  or (r_mut.status = 'INVALID')  or (nvl(r_mut.versie,'leeg') != nvl(nvl(r_mut.comments,r_mut.package_revision),'leeg')) then
      gen_utl_pck.log_msg('   record in de versietabel beeindigen : '|| r_mut.object_type|| ' '|| r_mut.owner|| ' '|| r_mut.object_name || ' versie ['|| r_mut.versie || ']');

      update chk_objectversies v
      set v.einddatum = l_rundatum
      where v.einddatum is null
      and v.owner = r_mut.owner
      and v.object_name = r_mut.object_name
      and v.object_type = r_mut.object_type;
    end if;

    /* nieuw record inserten alleen in geval van MUT
    en
    - record bestaat nog niet (v_owner is null)
    of
    - versienummer is anders
    */
    if (r_mut.bron = 'MUT') and ((l_is_aangepast and (nvl(r_mut.versie,'leeg') != nvl(nvl(r_mut.comments,r_mut.package_revision),'leeg')))
                             or  (r_mut.v_owner is null)) then

      gen_utl_pck.log_msg('   record in de versietabel inserten : '|| r_mut.object_type|| ' '|| r_mut.owner|| ' '|| r_mut.object_name || ' versie ['|| nvl(r_mut.comments,r_mut.package_revision) || ']');

      insert into chk_objectversies
      (  owner
      ,  object_name
      ,  object_type
      ,  created
      ,  last_ddl_time
      ,  versie
      ,  ingangsdatum
      ,  einddatum
      )
      values
      (  r_mut.owner
      ,  r_mut.object_name
      ,  r_mut.object_type
      ,  r_mut.created
      ,  r_mut.last_ddl_time
      ,  nvl(r_mut.comments,r_mut.package_revision)
      ,  l_rundatum
      ,  null
      );

      case r_mut.object_type
        when 'TABLE'
          then l_aantal_tab_mut := l_aantal_tab_mut + 1;
        when 'VIEW'
          then l_aantal_vw_mut := l_aantal_vw_mut + 1;
        when 'PACKAGE BODY'
          then l_aantal_pck_mut := l_aantal_pck_mut + 1;
         else
          null;
      end case;
    end if;
    if l_is_aangepast then
      if r_mut.status = 'INVALID' then
        gen_utl_pck.log_msg('  Dit object is invalid geworden! : '|| r_mut.object_type|| ' '|| r_mut.owner|| ' '|| r_mut.object_name || ' was ['|| r_mut.versie || ']');
        l_fouten_aanwezig := true;
        dbms_lob.append(l_fouten_clob,'- invalid geworden!! : '|| r_mut.object_type|| ' '|| r_mut.owner|| ' '|| r_mut.object_name || ' was ['|| r_mut.versie || ']'||l_regeleinde);
      elsif r_mut.versie is not null and nvl(r_mut.comments,r_mut.package_revision) is null then
        gen_utl_pck.log_msg('  Het versienummer is leeggemaakt! : '|| r_mut.object_type|| ' '|| r_mut.owner|| ' '|| r_mut.object_name || ' was ['|| r_mut.versie || '] is geworden ['|| nvl(r_mut.comments,r_mut.package_revision) || ']');
        l_fouten_aanwezig := true;
        dbms_lob.append(l_fouten_clob,'- versienummer leeggemaakt!! : '|| r_mut.object_type|| ' '|| r_mut.owner|| ' '|| r_mut.object_name || ' was ['|| r_mut.versie || '] is geworden ['|| nvl(r_mut.comments,r_mut.package_revision) || ']'||l_regeleinde);
      elsif r_mut.versie is null and nvl(r_mut.comments,r_mut.package_revision) is not null then
        gen_utl_pck.log_msg('  Het versienummer is gevuld, OK! : '|| r_mut.object_type|| ' '|| r_mut.owner|| ' '|| r_mut.object_name || ' was ['|| r_mut.versie || '] is geworden ['|| nvl(r_mut.comments,r_mut.package_revision) || ']');
      elsif nvl(r_mut.versie,'leeg') > nvl(nvl(r_mut.comments,r_mut.package_revision),'leeg') then
        gen_utl_pck.log_msg('  Het versienummer is verlaagd!! : '|| r_mut.object_type|| ' '|| r_mut.owner|| ' '|| r_mut.object_name || ' was ['|| r_mut.versie || '] is geworden ['|| nvl(r_mut.comments,r_mut.package_revision) || ']');
        l_fouten_aanwezig := true;
        dbms_lob.append(l_fouten_clob,'- versienummer verlaagd!! : '|| r_mut.object_type|| ' '|| r_mut.owner|| ' '|| r_mut.object_name || ' was ['|| r_mut.versie || '] is geworden ['|| nvl(r_mut.comments,r_mut.package_revision) || ']'||l_regeleinde);
      end if;
    end if;
  end loop;

  insert into chk_vsn_runs
  (owner
  ,rundatum
  ,aantal_tab_mut
  ,aantal_vw_mut
  ,aantal_pck_mut
  )
  values
  (user
  ,l_rundatum
  ,l_aantal_tab_mut
  ,l_aantal_vw_mut
  ,l_aantal_pck_mut
  );

  gen_utl_pck.log_msg(p_melding => l_functie||' Einde' );

  if l_fouten_aanwezig then
    select global_name
    into l_global_name
    from global_name;

    dbms_lob.append(l_fouten_clob,'Dit kan zo de bedoeling zijn, maar dit moet uitgezocht worden.');

    gen_mail_pck.verzend_email
    ( p_zender     => 'CHK_CSN_PCK@'||l_global_name
    , p_to         => 'ictderdelijnoracle@tkppensioen.nl'
    --, p_mime_type  => 'text/html'
    , p_onderwerp  => 'Objectversie problemen op database '||l_global_name|| ' user '||user
    , p_bericht    => l_fouten_clob
    );
    --l_afzender

  end if;
  --
  exception
  when others then
    gen_utl_pck.log_msg(p_melding => l_functie||'   Fout : '||sqlerrm );
end;
/* Haal directory(naam) op uit gen.instellingen. Default is utl_file_dir */

/* Start controle info rapportage job. */
procedure start_job
 is
/*
      Auteur: Alexander Wilten
      Datum:  13-11-2014
      Versie: chk 1.00.000
      Omschrijving:
        Start een oracle job die periodiek de controle op objectversies draait.

    ----------------------------------------------------------
      Wijzigingen
    ----------------------------------------------------------
      Auteur:
      Datum:
      Versie:
      Wijziging:
        W xx xxxx

    ----------------------------------------------------------
    */
    l_start_tijd         varchar2(10);
    l_job_procedure      varchar2(60) := 'CHK_VSN_PCK.VASTLEGGEN_OBJECTVERSIES';
    l_functie            varchar2(60) := 'chk_vsn_pck.start_job : ';
    l_dummy_job_name     varchar2(60);
    l_job_already_exists exception;
begin
  gen_utl_pck.log_msg( p_melding => l_functie || 'begin functie');

  -- bepalen van de start datum + tijd
  gen_utl_pck.log_msg( p_melding => l_functie || '  starttijd : '||to_char(sysdate, 'dd-mm-yyyy hh24:mi' ) );
  -- controleer of de job al loopt
  begin
    select job.job_name
    into   l_dummy_job_name
    from   all_scheduler_jobs job
    where  upper(job.job_name) = g_job_name||user;
    -- indien job gevonden, loopt deze al, en dan niets doen
    raise l_job_already_exists;
  exception
    when no_data_found then
      -- oke, de job bestaat nog niet
      null;
      gen_utl_pck.log_msg( p_melding => l_functie || '  job niet gevonden, nieuwe wordt gescheduled');
  end;
  -- creeer een oracle scheduler job
  dbms_scheduler.create_job( job_name            => g_job_name||user
                           , job_type            => 'PLSQL_BLOCK'
                           , job_action          => 'begin '||l_job_procedure||'; end;'
                           , number_of_arguments => 0
                           , start_date          => sysdate
                           , repeat_interval     => 'freq=minutely ; interval=30'
                           , end_date            => null
                           , job_class           => 'DEFAULT_JOB_CLASS'
                           , auto_drop           => false
                           , comments            => 'periodieke controle op objectversies');

  -- start de job
  dbms_scheduler.enable(name => g_job_name||user);
  gen_utl_pck.log_msg( p_melding => l_functie || '  job ['||g_job_name||user ||'] gescheduled');
  gen_utl_pck.log_msg( p_melding => l_functie || 'einde functie');
  --
exception
  when l_job_already_exists then
    gen_utl_pck.log_msg( p_melding => l_functie || 'job ['||g_job_name||user||'] draait al');

  when others then
    gen_utl_pck.log_msg( p_melding => l_functie || sqlerrm, p_item => 'ERROR');
    raise;
end;
/* Stop controle info rapportage job */
procedure stop_job
 is
/*
      Auteur: Alexander Wilten
      Datum:  13-11-2014
      Versie: chk 1.00.000
    Omschrijving:
      Stop de oracle job die periodiek de controle op objectversies draait.

    ----------------------------------------------------------
      Wijzigingen
    ----------------------------------------------------------
      Auteur:
      Datum:
      Versie:
      Wijziging:
        W xx xxxx

   ----------------------------------------------------------
   */
  l_dummy_job_name    varchar2(60) := '';
  l_functie           varchar2(60) := 'chk_vsn_pck.stop_job : ';
begin
    --
    gen_utl_pck.log_msg( p_melding => l_functie || 'begin functie');
    --
    begin
      --
      select job.job_name
      into   l_dummy_job_name
      from   all_scheduler_jobs job
      where  upper(job.job_name) = g_job_name||user;

      gen_utl_pck.log_msg( p_melding => l_functie || '  job ['||l_dummy_job_name||'] gevonden. Verwijderen');
      -- job is gevonden, dus verwijderen
      dbms_scheduler.drop_job
        ( job_name   =>  l_dummy_job_name,
          force      =>  true);
      --
      gen_utl_pck.log_msg( p_melding => l_functie || '  job verwijderd: [' || g_job_name||user ||']');
      --
    exception
      when no_data_found then
        -- oke, job bestaat niet
        gen_utl_pck.log_msg( p_melding => l_functie || 'job bestaat niet :[' || g_job_name||user ||']');
    end;
    gen_utl_pck.log_msg( p_melding => l_functie || 'einde functie');
  exception
    when others then
      gen_utl_pck.log_msg( p_melding => l_functie || sqlerrm, p_item => 'ERROR');
      raise;
  end;

FUNCTION DEPENDENCIES
 (P_OWNER IN varchar2
 ,P_NAME IN varchar2
 ,P_TYPE IN varchar2
 )
 RETURN sys_refcursor
 IS
type r_objects is record ( referenced_owner   varchar2(32767)
                         , referenced_name    varchar2(32767)
                         , referenced_type    varchar2(32767) );
type t_objects is table of r_objects index by binary_integer;
l_ref_cursor     sys_refcursor;
l_dependencies   t_dependencies := t_dependencies();
g_stack          varchar2(32767) := ';';

   procedure recurs( p_owner in varchar2
                   , p_name in varchar2
                   , p_type in varchar2
                   , p_level in integer default 0 ) is
   str_representation   varchar2(32767) := p_owner || Chr(9) || p_name || Chr(9) || p_type;
   l_objects            t_objects;
   begin
      if instr(g_stack, ';' || str_representation || ';') = 0 then
         g_stack := g_stack || str_representation || ';';
         l_dependencies.extend(1);

         declare
         str_versie   varchar2(32767);
         begin
            select versie into str_versie
            from chk_objectversies
            where owner       = p_owner
            and   object_name = p_name
            and   object_type = p_type
            and   ( ingangsdatum <= sysdate and ( einddatum > sysdate or einddatum is null ) );

            l_dependencies(l_dependencies.count) := str_representation || Chr(9) || str_versie;

         exception
            when NO_DATA_FOUND then
               l_dependencies(l_dependencies.count) := str_representation;
         end;

         for x in ( select referenced_owner                                                    referenced_owner
                         , referenced_name                                                     referenced_name
                         , decode(referenced_type, 'PACKAGE BODY', 'PACKAGE', referenced_type) referenced_type
                    from sys.dba_dependencies
                    where owner                                         = p_owner
                    and   name                                          = p_name
                    and   decode(type, 'PACKAGE BODY', 'PACKAGE', type) = p_type
                    and   referenced_owner != 'SYS' )
         loop
            l_objects(l_objects.count).referenced_owner := x.referenced_owner;
            l_objects(l_objects.last).referenced_name   := x.referenced_name;
            l_objects(l_objects.last).referenced_type   := x.referenced_type;
         end loop;

         declare
         l_index   binary_integer := l_objects.first;
         begin
            while l_index is not null loop
               recurs(l_objects(l_index).referenced_owner, l_objects(l_index).referenced_name, l_objects(l_index).referenced_type, p_level+1);
               l_index := l_objects.next(l_index);
            end loop;
         end;
      end if;
   end;
begin
   recurs(p_owner, p_name, p_type);

   open l_ref_cursor for
      select column_value dependency
      from table(cast(l_dependencies as t_dependencies))
      order by 1;

   return l_ref_cursor;
END DEPENDENCIES;
FUNCTION DEPENDENCIES_UNKNOWN_OWNER
 (P_NAME IN varchar2
 ,P_TYPE IN varchar2
 )
 RETURN sys_refcursor
 IS
begin
   for owner in ( select owner
                  from sys.dba_objects
                  where object_name = p_name
                  and   object_type = p_type
                  order by decode(owner, 'PAS', 1, 'ALG', 2, 3) )
   loop
      return dependencies(owner.owner, p_name, p_type);
   end loop;
   return null;
END DEPENDENCIES_UNKNOWN_OWNER;
end chk_vsn_pck;
/

