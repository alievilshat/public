package org.cs3.jlmp.internal.astvisitor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.cs3.pl.common.Debug;
import org.eclipse.jdt.core.dom.*;

/**
 * Constructs IDs out of the different AST elements, offering the 
 * functionality promised by the IIDResolver interface. This 
 * implementation works very close to the AST, using Binding information
 * whereever possible. It will therefore not be suited for incomplete
 * ASTs where such binding information is usually unavailable.
 *  
 * @author schulzs1
 */

public class IDResolver implements IIDResolver {
	
	protected HashMap localBindings = new HashMap();
	protected HashMap knownIDs = new HashMap();
		
	protected FQNTranslator fqnManager;
	protected IIDGenerator provider;
	

	/**
	 * Contructs a new IDResolver Object.
	 * @param localIds if true, every ID is wrapped by the term lId/1. 
	 * A global Id will be resolved by inTe/2. 
	 * @deprecated use IDResolver(FQNManager, IIDProvider)
	 */
	
	public IDResolver(boolean localIds) {
		this(new IdentityFQNTranslator(), localIds? 
				(IIDGenerator) new LocalIDGenerator() : 
				(IIDGenerator) new SimpleIDGenerator());
	}
	
	/**
	 * constructs a new IDResolver Object, with a fixed starting value.
	 * @param next the first ID to be returned
	 * @param localIds should local IDs be used?
	 * @deprecated use IDResolver(FQNManager, IIDProvider)
	 */
	
	public IDResolver(int next, boolean localIds) {
		if (localIds)
			provider = new LocalIDGenerator(next);
		else
			provider = new SimpleIDGenerator(next);
		
		fqnManager = new IdentityFQNTranslator();
	}
	
	/**
	 * Constructs an IDResolver Object. The Resolver will get its id-supply
	 * from the passed IIDProvider, and use the provider's scheme. This
	 * enables us to transparently use multiple schemes for non-global
	 * IDs without putting complexity into the resolver. The FQNManager
	 * is used to transform the fqn(...) terms generated by the resolver
	 * into the preferred format.
	 * 
	 * @param fqn an FQNManager instance
	 * @param provider an IIDProvider instance
	 */
	
	public IDResolver(FQNTranslator fqn, IIDGenerator provider) {
		this.fqnManager = fqn;
		this.provider = provider;
	}

	/**
	 * Creates a new unique ID for use in the FactGenerator, if there is a 
	 * node that needs to be split into several.
	 */
	
	public String getID(){
		return provider.getID();
	}
	
	/**
	 * getID creates a new unique ID for an ASTNode, that is unchanging for
	 * further calls, and is either an Integer, for nodes with local
	 * visibility, or a term of the form fqn(...) for the interface elements,
	 * or an lId() term if the IDResolver is supposed to generate local
	 * ids
	 * 
	 * @param node The node to create an ID for
	 * @return an unique identifier
	 */
	public String getID(ASTNode node) {
		String rv = "'null'";
		if (knownIDs.containsKey(node)) {
			return knownIDs.get(node).toString();
		}
		if (node == null) {
			knownIDs.put(node, rv);
			return rv;
		}
		if (node instanceof PackageDeclaration) {
			PackageDeclaration pd = (PackageDeclaration) node;
			rv = getID(pd.resolveBinding());
		} else if (node instanceof TypeDeclaration) {
			TypeDeclaration td = (TypeDeclaration) node;
			if(td.getName().toString().equals("Subroutine")){
			    Debug.debug("debug");
			}
			ITypeBinding binding = td.resolveBinding();
            if (td.getParent() instanceof AnonymousClassDeclaration) {
				rv = provider.getID();
				localBindings.put(binding, rv);
			}else if (td.isLocalTypeDeclaration()) {
				rv = provider.getID();
				localBindings.put(binding, rv);
			} else {
				rv = getID(binding);
			}
			//ld: AnonymousClassDeclaration != TypeDeclaration !!!!!!!!!!!
		}else if (node instanceof AnonymousClassDeclaration) {
			AnonymousClassDeclaration td = (AnonymousClassDeclaration) node;
			rv = provider.getID();
			localBindings.put(td.resolveBinding(), rv);
			
		} else if (node instanceof VariableDeclarationFragment) {
			VariableDeclarationFragment vdf = (VariableDeclarationFragment) node;
			if (node.getParent() instanceof FieldDeclaration && 
					!isMemberOfLocalClass(node.getParent())) {
				rv = getID(vdf.resolveBinding());
			} else {
				rv = provider.getID();
				localBindings.put(vdf.resolveBinding(), rv);
			}
			
		}else if (node instanceof SingleVariableDeclaration) {
			SingleVariableDeclaration vdf = (SingleVariableDeclaration) node;
			String id = provider.getID();
			localBindings.put(vdf.resolveBinding(), id);
			rv = id;						
		} else if (node instanceof MethodDeclaration) {
			if(isMemberOfLocalClass(node)) {
				rv = provider.getID();
				localBindings.put(((MethodDeclaration)node).resolveBinding(), rv);
			} else {
				MethodDeclaration md = (MethodDeclaration) node;
				rv = getID(md.resolveBinding());
			}
		} else {
			rv = provider.getID();
		}
		knownIDs.put(node, rv);
		return rv;
	}
	
	/**
	 * Checks if <code>member</code> is a member of a local (anomynous) class.  
	 * 
	 * @param member
	 * @return true if member is local
	 */
	private boolean isMemberOfLocalClass(ASTNode member) {
		
		if(member.getParent() instanceof TypeDeclaration)
			return ((TypeDeclaration)member.getParent()).isLocalTypeDeclaration();
		if(member.getParent() instanceof AnonymousClassDeclaration)
			return true;
		throw new IllegalArgumentException("Not implemented for node " + member +".");
	}
	
		
	/**
	 * Resolves an entire List of Nodes or IBinding Object, calling
	 * getID(IBinding) and getID(ASTNode) for each member of the List
	 * 
	 * @param nodes
	 *            A List of Nodes or Bindings
	 * @return A String, which is a valid Prolog List containing the IDs of the
	 *         members, in the same order as in the passed list.
	 * @throws IllegalArgumentException
	 *             Not all List Nodes are of valid type
	 */
	public String getIDs(List nodes) {
		if (nodes == null)
			return "[]";
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		Iterator it = nodes.iterator();
		boolean first = true;
		while (it.hasNext()) {
			if (!first)
				sb.append(", ");
			first = false;
			Object o = it.next();
			if (o instanceof ASTNode) {
				sb.append(getID((ASTNode) o));
			} else if (o instanceof IBinding) {
				sb.append(getID((IBinding) o));
			} else {
				throw new IllegalArgumentException(
						"Not all List members are ASTNodes or IBindings");
			}
		}
		sb.append("]");
		return sb.toString();
	}
	
	/**
	 * Resolves an IBinding Object to its bound location, either as an
	 * fqn()-Term or, for local variables, the numeric ID of its declaration,
	 * or an lId-Term if selected.
	 * 
	 * @param iface
	 *            The IBinding Object to be mapped to a String
	 * @return A string, either an numeric ID or an fqn-Term
	 */
	public String getID(IBinding iface) {
		if (iface == null)
			throw new IllegalArgumentException("Binding was not resolved");
		
		/* StS: Hotfix: Local bindings first! */
		
		if (localBindings.containsKey(iface))
			return localBindings.get(iface).toString();
		
		int kind = iface.getKind();
		StringBuffer buff = new StringBuffer();
		buff.append("fqn('");
		String key;
		switch (kind) {
			case IBinding.PACKAGE :
				IPackageBinding pb = (IPackageBinding) iface;
				buff.append(normalizeFullQualifiedName(pb.getKey()));
				buff.append("'");
				break;
			case IBinding.METHOD :
				IMethodBinding mb = (IMethodBinding) iface;
				if (mb.getDeclaringClass().isLocal()) {
					String resolved = provider.getID();
					localBindings.put(mb, resolved);
					return resolved;
				}
				buff.append(normalizeFullQualifiedName(mb.getDeclaringClass().getQualifiedName()));
				buff.append("', '");
				if (mb.isConstructor())
					buff.append("<init>");
				else
					buff.append(mb.getName());
				buff.append("', [");
				boolean first = true;
				ITypeBinding[] params = mb.getParameterTypes();
				for (int i = 0; i < params.length; i++) {
					ITypeBinding b = params[i];
					if (!first)
						buff.append(", '");
					else
						buff.append("'");
					buff.append(b.getQualifiedName());
					first = false;
					buff.append("'");
				}
				buff.append("]");
				break;
			case IBinding.TYPE :
				ITypeBinding tb = (ITypeBinding) iface;
                
			if(tb.getName().equals("Subroutine")){
			    Debug.debug("debug");
			}
//				if (tb.isTopLevel())
//					buff.append(tb.getQualifiedName());
//				else if (tb.isMember()) {
//					StringBuffer tmp = new StringBuffer();
//					ITypeBinding father = tb.getDeclaringClass();
//					ITypeBinding last = father;
//					tmp.append(tb.getName());
//					while (father != null) {
//						last = father;
//						tmp.insert(0, father.getName() + "$"); //windeln: replaced $
//						father = father.getDeclaringClass();
//					}
//					if(!last.getPackage().isUnnamed())
//					tmp.insert(0, last.getPackage().getName() + ".");
//					buff.append(tmp);
//				} else {
//					// local class or anonymous class
//					Integer theID = (Integer) localBindings.get(tb);
//					
//					return theID.toString();					
//				}
			buff.append(normalizeFullQualifiedName(tb.getKey()));
				buff.append("'");
				break;
			case IBinding.VARIABLE :
							
				IVariableBinding vb = (IVariableBinding) iface;
				if (vb.isField()) {

					// We can construct a fqn(Class, Name)
					if(vb.getDeclaringClass() == null){
						//TODO: ld: what to do about <xyz>[].length ?????
						return "'null'";
					}
					
					/*StS: Hotfix for missing members in anonymous classes */
										
					if (vb.getDeclaringClass().isLocal()){
						String resolved = provider.getID();
						localBindings.put(vb, resolved);
						return resolved;
					}
					
					
					buff.append(vb.getDeclaringClass().getQualifiedName());
					buff.append("', '");
					buff.append(vb.getName());
					buff.append("'");
				} else {
					Integer theID = (Integer) localBindings.get(vb);					
					return theID.toString();
				}
				break;
			default :
				throw new IllegalArgumentException("Unknown kind of Binding");
		}
		buff.append(")");
		return fqnManager.transformFQN(buff.toString());
	}

	/**
	 * @param key
	 * @return
	 */
	static String normalizeFullQualifiedName(String key) {
		if(key.endsWith(";") && key.startsWith("L"))
			key = key.substring(1,key.length()-1);
		return key.replace('/','.');
	}

	/**
	 * Makes n1 and n2 equivalent. This means that each call to getID()
	 * will return the same ID for both of them. This method must be called
	 * <u>before</u> both of them have been assigned their own IDs. If 
	 * only one of them has an ID, this one is used for all further calls.
	 * 
	 * The nodes must not be equal, and neither may be null.
	 * 
	 * @param n1 an ASTNode
	 * @param n2 an ASTNode
	 * @exception IllegalArgumentException bad (null or equal) arguments
	 * @exception IllegalStateException both nodes were already mapped
	 */
	
	public void setEquivalence(ASTNode n1, ASTNode n2) {
		String id;
		if (n1 == n2)
			throw new IllegalArgumentException("n1 must be != n2");
		if (n1 == null || n2 == null)
			throw new IllegalArgumentException("No argument may be null");
		
		if (knownIDs.containsKey(n1) && knownIDs.containsKey(n2))
			throw new IllegalStateException("Both nodes are already mapped");
		
		if (!knownIDs.containsKey(n1) && !knownIDs.containsKey(n2)){
			id = getID(n1);
			knownIDs.put(n1, id);
			knownIDs.put(n2, id);
		} else if (knownIDs.containsKey(n1)){
			id = knownIDs.get(n1).toString();
			knownIDs.put(n2, id);
		} else { 
			id = knownIDs.get(n2).toString();
			knownIDs.put(n1, n2);
		}
	}
	/**
	 * returns the ID given to the class <code>java.lang.Object</code>. 
	 * This method is used because of the many and varied codings for
	 * globally unique names we have used up till now, so the other
	 * classes need not assume any specific implementation
	 * 
	 * @see org.cs3.jlmp.internal.astvisitor.IIDResolver#getJavaLangObjectID()
	 */
	public String getJavaLangObjectID() {
		return fqnManager.transformFQN("fqn('java.lang.Object')");
	}

	/**
	 * returns the ID given to the class <code>java.lang.Object</code>. 
	 * This method is used because of the many and varied codings for
	 * globally unique names we have used up till now, so the other
	 * classes need not assume any specific implementation
	 * 
	 * @see org.cs3.jlmp.internal.astvisitor.IIDResolver#getJavaLangObjectID()
	 */
	
	public String getJavaLangClassID() {
		return fqnManager.transformFQN("fqn('java.lang.Class')");
	}

    /* (non-Javadoc)
     * @see org.cs3.jlmp.internal.astvisitor.IIDResolver#getSyntheticConstructorID(org.eclipse.jdt.core.dom.ITypeBinding)
     */
    public String getSyntheticConstructorID(ITypeBinding tb) {
       
        String key =tb==null? "java.lang.Object" :  IDResolver.normalizeFullQualifiedName(tb.getKey());

        String fqn = "fqn('" + key + "', '<init>', [])";       
        return fqn;
    }
}
