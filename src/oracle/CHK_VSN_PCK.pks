-- N:\Lveekhout\CHK_VSN_PCK.pks

CREATE OR REPLACE PACKAGE ALG.CHK_VSN_PCK authid current_user IS


/* package om versies van objecten bij te houden*/

/* Return package revision label */
FUNCTION REVISION
 RETURN VARCHAR2;
PRAGMA RESTRICT_REFERENCES (REVISION, WNDS, WNPS);
function  get_package_revision(p_owner varchar,p_naam varchar,p_type varchar)
return varchar2;
/* Vastleggen van de objectversies in de tabel */
PROCEDURE VASTLEGGEN_OBJECTVERSIES;
/* controle op nog te verwerken rapportage verzoeken */
PROCEDURE START_JOB;
/* Stop controle info rapportage job */
PROCEDURE STOP_JOB;
FUNCTION DEPENDENCIES
 (P_OWNER IN varchar2
 ,P_NAME IN varchar2
 ,P_TYPE IN varchar2
 )
 RETURN sys_refcursor;
FUNCTION DEPENDENCIES_UNKNOWN_OWNER
 (P_NAME IN varchar2
 ,P_TYPE IN varchar2
 )
 RETURN sys_refcursor;
END CHK_VSN_PCK;
/

