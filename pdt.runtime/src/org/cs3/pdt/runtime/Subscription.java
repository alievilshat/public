/*****************************************************************************
 * This file is part of the Prolog Development Tool (PDT)
 * 
 * Author: Lukas Degener (among others) 
 * E-mail: degenerl@cs.uni-bonn.de
 * WWW: http://roots.iai.uni-bonn.de/research/pdt 
 * Copyright (C): 2004-2006, CS Dept. III, University of Bonn
 * 
 * All rights reserved. This program is  made available under the terms 
 * of the Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * In addition, you may at your option use, modify and redistribute any
 * part of this program under the terms of the GNU Lesser General Public
 * License (LGPL), version 2.1 or, at your option, any later version of the
 * same license, as long as
 * 
 * 1) The program part in question does not depend, either directly or
 *   indirectly, on parts of the Eclipse framework and
 *   
 * 2) the program part in question does not include files that contain or
 *   are derived from third-party work and are therefor covered by special
 *   license agreements.
 *   
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *   
 * ad 1: A program part is said to "depend, either directly or indirectly,
 *   on parts of the Eclipse framework", if it cannot be compiled or cannot
 *   be run without the help or presence of some part of the Eclipse
 *   framework. All java classes in packages containing the "pdt" package
 *   fragment in their name fall into this category.
 *   
 * ad 2: "Third-party code" means any code that was originaly written as
 *   part of a project other than the PDT. Files that contain or are based on
 *   such code contain a notice telling you so, and telling you the
 *   particular conditions under which they may be used, modified and/or
 *   distributed.
 ****************************************************************************/

package org.cs3.pdt.runtime;

import org.cs3.pl.prolog.PrologInterface;

/**
 * Subscription for a PrologInterface instance.
 * 
 * Subscriptions can best be thought of as describing a particular use some
 * client makes of a given PrologInterface instance. The pdt.core for example
 * makes use of Subscriptions to describe the relation of a IPrologProject to
 * the PrologInterface instances it uses. On the one hand, a PrologInterface is
 * used as the default runtime for the user application developed in the
 * project. On the other hand, the core itself internally needs a
 * PrologInterface to store metadata on the prolog source code and other
 * information associated to the project. So there are two Subscriptions
 * associated with each Prolog project.
 * 
 * Note that several subscriptions may be to one and the same PrologInterface.
 * In the above example, this is configurable by the user. On the other hand the
 * relation between projects and Subscriptions is hard coded. This does make sense: every 
 * prolog project makes use of prolog in exactly the ways described above - there is no need to
 * configure anything. 
 * 
 * @author lukas
 * 
 */
public interface Subscription {
	/**
	 * @return a unique identifier for this subscription, or null for anonymous
	 *         subscriptions.
	 */
	public abstract String getId();

	/**
	 * @return a key identifying the PrologInterface instance to subscribe to.
	 *         May NOT be null.
	 */
	public abstract String getPifKey();

	/**
	 * @return a short human readable text describing what the subscription is
	 *         used for. E.g. "Used to store metadata about prolog code found in
	 *         project FooBar." Maybe null.
	 */
	public abstract String getDescritpion();

	/**
	 * @return The name of this Subscription. Will be used as label string in
	 *         the UI. maybe null. This should be something short and catchy
	 *         like "FooBar - metadata".
	 */
	public abstract String getName();

	/**
	 * "configure-your-pif-here"-hook. Called by the runtime at the earliest
	 * possible point in time that does satisfy both of the following
	 * conditions:
	 *  - the pif has been instantiated. 
	 *  - this subscription has been registered
	 * with the PrologInterfaceRegistry.
	 * 
	 * Note that this method is never called for anonymous subscriptions. This
	 * is implied by the second condition.
	 * 
	 * Implementation should make no assumptions on the state of the pif
	 * argument: It may be up and running. It may be not. Best is to check the
	 * state and start the pif if it is not up.
	 * 
	 * @param pif
	 */
	public abstract void configure(PrologInterface pif);

	/**
	 * "clean-up-your-mess"-hook.
	 * Called by the runtime when the subscription is removed from the registry, or
	 * when a pif to which the client subscribed is removed from the registry
	 * 
	 * not called on anonymous subscriptions (see above)
	 * 
	 * 
	 * Implementation should make no assumptions on the state of the pif
	 * argument: It may be up and running. It may be not. Best is to check the
	 * state. If the pif is down, there is typically not much to clean up anyway.
	 * @param prologInterface
	 */
	public abstract void deconfigure(PrologInterface pif);
}