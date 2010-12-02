%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% This file is part of the Prolog Development Tool (PDT)
% 
% Author: Lukas Degener (among others) 
% E-mail: degenerl@cs.uni-bonn.de
% WWW: http://roots.iai.uni-bonn.de/research/pdt 
% Copyright (C): 2004-2006, CS Dept. III, University of Bonn
% 
% All rights reserved. This program is  made available under the terms 
% of the Eclipse Public License v1.0 which accompanies this distribution, 
% and is available at http://www.eclipse.org/legal/epl-v10.html
% 
% In addition, you may at your option use, modify and redistribute any
% part of this program under the terms of the GNU Lesser General Public
% License (LGPL), version 2.1 or, at your option, any later version of the
% same license, as long as
% 
% 1) The program part in question does not depend, either directly or
%   indirectly, on parts of the Eclipse framework and
%   
% 2) the program part in question does not include files that contain or
%   are derived from third-party work and are therefor covered by special
%   license agreements.
%   
% You should have received a copy of the GNU Lesser General Public License
% along with this program; if not, write to the Free Software Foundation,
% Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
%   
% ad 1: A program part is said to "depend, either directly or indirectly,
%   on parts of the Eclipse framework", if it cannot be compiled or cannot
%   be run without the help or presence of some part of the Eclipse
%   framework. All java classes in packages containing the "pdt" package
%   fragment in their name fall into this category.
%   
% ad 2: "Third-party code" means any code that was originaly written as
%   part of a project other than the PDT. Files that contain or are based on
%   such code contain a notice telling you so, and telling you the
%   particular conditions under which they may be used, modified and/or
%   distributed.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

:-module(pdt_util,[
	pdt_call_cleanup/2,
	pdt_file_spec/2,
	pdt_file_spec/3,
	pdt_file_ref/2,
	pdt_member/2,
	pdt_memberchk/2,	
	pdt_chop_before/3,
	pdt_chop_after/3,
	pdt_chop_before/4,
	pdt_chop_after/4,
	pdt_remove_duplicates/2,
	pdt_count/2,
	pdt_unique/2,
	pdt_hidden_path/1,
	pdt_first/2,	
	has_tail/2
]).

:- use_module(library('/org/cs3/pdt/util/pdt_preferences')).

:-module_transparent pdt_count/2.
pdt_count(Goal,N):-
    nb_setval('pdt_util:pdt_count$counter',0),
    forall(Goal,
    	(	nb_getval('pdt_util:pdt_count$counter',Old),
    		New is Old +1,
    		nb_setval('pdt_util:pdt_count$counter',New)
    	)
    ),
    nb_getval('pdt_util:pdt_count$counter',N).


/*
as the implementation of the sys pred call_cleanup/2 seems to be
broken, here is our own impl.
*/

pdt_call_cleanup(Goal,Cleanup):-
    catch(Goal,E,true),!,
    Cleanup,
    (	nonvar(E)
    ->	throw(E)
    ;	true
    ).
pdt_call_cleanup(_,Cleanup):-    
    Cleanup,
    fail.


has_tail(Tail,Tail):-!.
has_tail(Tail,[_|Mid]):-
	has_tail(Tail,Mid).



%% pdt_file_spec(+FileSpec, -Abs)
%
% pdt "standard" procedure for resolving file specifications
% to absolute file names.
% 
% @param FileSpec can be a filename, an aliased file names or a file references, i.e. a term of the form 
% =file_ref(<integer>)=. See pdt_file_ref/2.
% @param Abs will be unified with a canonical, absolute path.
		
pdt_file_spec(Var, Abs):-
    var(Var),nonvar(Abs),!,
    Var=Abs.
pdt_file_spec(file_ref(Ref), Abs):-
	fileref(Abs,Ref).
pdt_file_spec(FileSpec, Abs):-
	filespec(FileSpec, Abs).


pdt_file_spec(FileSpec,Base, Abs):-
	filespec(FileSpec,Base, Abs).


filespec(FileSpec,Base, Abs):-
	absolute_file_name(FileSpec,[relative_to(Base),solutions(all),file_errors(fail),extensions(['.pl','.ct','']),access(read)],Abs),
	\+ hidden(Abs).

filespec(FileSpec, Abs):-
	absolute_file_name(FileSpec,[solutions(all),file_errors(fail),extensions(['.pl','.ct','']),access(read)],Abs),
	\+ hidden(Abs).



:- pdt_add_preference(
	ignore_hidden_libs,
	'Hide PDT libraries', 
	'If true, pdt_file_spec will not retrieve any absolute paths starting with a prefix marked as hidden.
	 To mark a particular prefix as hidden, add clauses to the dynamic predicate pdt_hidden_path/1.
	 The PDT by default marks all its libraries as hidden. In situations when you are working
	 with PDT source code and do not want to mix it up with the code loaded by the PDT core, it may be usefull 
	 to turn on this option. By default it is set to false.',
	false
).


:- dynamic pdt_hidden_path/1.
:- multifile pdt_hidden_path/1.

hidden(Abs):-
	pdt_preference_value(ignore_hidden_libs,true),
	pdt_hidden_path(Prefix),
	atom_concat(Prefix,_,Abs),
	!.    
	
:-dynamic fileref/2.
:-index(fileref(1,1)).


%% pdt_file_ref(?FileSpec, ?Integer)
% get a file specification for reference number or vice versa.
% If no reference number exists for a given file, it will be created.
pdt_file_ref(Abs,Ref):-
    nonvar(Ref),
	!,
	fileref(Abs,Ref).	
pdt_file_ref(file_ref(Ref),Ref):-
	!.
pdt_file_ref(FileSpec,Ref):-
	fileref(FileSpec,Ref),
	!.
pdt_file_ref(FileSpec,Ref):-
   	filespec(FileSpec,Abs),
	fileref(Abs,Ref),
	!.
pdt_file_ref(FileSpec,Ref):-
	filespec(FileSpec,Abs),
	gen_fileref(Ref),
	assert(fileref(Abs,Ref)).


pdt_gen_virtual_file_ref(Ref):-
    gen_fileref(Ref).
	
gen_fileref(Ref):-
    flag(pdt_annotator_current_file_ref,Ref,Ref+1).
    
    


%% pdt_member(?Member,+List).
%    true if Member is an element of List.
%    
%    Redefines builtin member/2 to handle lists with an unbound tail.
%    Like member/2 this predicate will bind unbound elements of the list.
%    Unlike member/2, this predicate will NOT bind an unbound tail.
	
pdt_member(_,Var):-
    var(Var),!,fail.
pdt_member(M,[M|_]).
pdt_member(M,[_|T]):-
    pdt_member(M,T).

pdt_memberchk(M,L):-
	pdt_member(M,L),
	!.    
    
%% pdt_chop_before(+Elm,+Elms,+Suffix)
%
% Elms should be an orderd list.
% Suffix will unified with the first suffix of Elms
% whos elements are equal or greater than Elm.
pdt_chop_before(Elm,[Elm|Elms],[Elm|Elms]):-
	!.
pdt_chop_before(Elm,[Head|NextElms],Elms):-
    Head@<Elm,!,
    pdt_chop_before(Elm,NextElms,Elms).
pdt_chop_before(_,Elms,Elms).

%% pdt_chop_after(+Elm,+Elms,+Suffix)
%
% Elms should be an orderd list.
% Suffix will unified with the first suffix of Elms
% whos elements are strictly greater than Elm.
pdt_chop_after(Elm,[Head|NextElms],Elms):-
    Head@=<Elm,!,
    pdt_chop_after(Elm,NextElms,Elms).
pdt_chop_after(_,Elms,Elms).
       
       
%% pdt_chop_before(+Elm,+Elms,+Suffix)
%
% Elms should be an orderd list.
% Suffix will unified with the first suffix of Elms
% whos elements are equal or greater than Elm.
pdt_chop_before(Elm,[Elm|Elms],[],[Elm|Elms]):-
	!.
pdt_chop_before(Elm,[Head|NextElms],[Head|NextPrefs],Elms):-
    Head@<Elm,!,
    pdt_chop_before(Elm,NextElms,NextPrefs,Elms).
pdt_chop_before(_,Elms,[],Elms).

%% pdt_chop_after(+Elm,+Elms,+Suffix)
%
% Elms should be an orderd list.
% Suffix will unified with the first suffix of Elms
% whos elements are strictly greater than Elm.
pdt_chop_after(Elm,[Head|NextElms],[Head|NextPrefs],Elms):-
    Head@=<Elm,!,
    pdt_chop_after(Elm,NextElms,NextPrefs,Elms).
pdt_chop_after(_,Elms,[],Elms).        
        
%% pdt_remove_duplicates(+In,-Out) 
%
% In should be an ordered List.
% Out will be unified with the duplicate-free version of In
pdt_remove_duplicates([],[]).
pdt_remove_duplicates([Elm,Elm|Elms],[Elm|DupFreeElms]):-
    !,
    pdt_remove_duplicates([Elm|Elms],[Elm|DupFreeElms]).
pdt_remove_duplicates([Elm|Elms],[Elm|DupFreeElms]):-
    !,
    pdt_remove_duplicates(Elms,DupFreeElms).

pdt_unique(Prefix,Unique):-
	atom_concat(unique,Prefix,Counter),
	flag(Counter,N,N+1),
	atom_concat(Prefix,N,Unique).  
	

:- module_transparent pdt_first/2.
pdt_first(Goal,Vars):-
	copy_term(Goal-Vars,GoalC-VarsC),
	Vars=VarsC,
	call(Goal),
	once(GoalC),
	Goal=@=GoalC.
