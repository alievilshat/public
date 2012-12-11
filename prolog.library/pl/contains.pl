/*****************************************************************************
 * This file is part of the Prolog Development Tool (PDT)
 * 
 * WWW: http://sewiki.iai.uni-bonn.de/research/pdt/start
 * Mail: pdt@lists.iai.uni-bonn.de
 * Copyright (C): 2004-2012, CS Dept. III, University of Bonn
 * 
 * All rights reserved. This program is  made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 ****************************************************************************/

:- use_module(library(lists)). 

%sublist(S, L) :-
%  append(_, L2, L),
%  append(S, _, L2).

contains(A, B) :-
  atom(A),
  atom(B),
  name(A, AA),
  name(B, BB),
  contains(AA, BB).

contains(A, B) :-
  atom(B),
  name(B, BB),
  contains(A, BB).
  
contains(A, B) :-
  atom(A),
  name(A, AA),
  contains(AA, B).

contains(A, B) :-
  sublist(B, A),
  B \= [].


