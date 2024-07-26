package vg.civcraft.mc.civmodcore.gluing;

/**
 * <p>How to create a dependency glue in N easy steps!</p>
 *
 * <p>Create a <code>glues</code> package somewhere in your project, then inside there, create a package for your
 * intended dependency, but lowercase, eg: <code>namelayer</code>. This is where you'll store <b>ALL</b> your classes
 * related to that dependency. This package should <b>NOT</b> be accessed anywhere else in your project. The only
 * exception is for other glues where you're absolutely certain it's safe, eg, a Citadel glue accessing the NameLayer
 * glue, since Citadel cannot function without NameLayer, thus Citadel won't ever be enabled without NameLayer also
 * being enabled.</p>
 *
 * <p>Next, create a class that implements <code>DependencyGlue</code> within your dependency's respective glue package,
 * eg: <code>glues/namelayer/NameLayerGlue.java</code>. This class will only be initialised when your dependency is
 * enabled.</p>
 *
 * <p>Next, create a <code>glues.properties</code> resource file. This file matches dependency names to glue class
 * paths, eg: <code>NameLayer:net.civmc.example.glues.NameLayerGlue</code>. This does mean you're limited to one glue
 * per dependency.</p>
 *
 * <p>Ensure that <code>CivModCore</code> AND your dependency's name (eg: <code>NameLayer</code>) is included within the
 * <code>depend</code> or <code>softdepend</code> lists of your plugin's <code>plugin.yml</code> resource file.</p>
 */
public interface DependencyGlue {
    void enable();
    void disable();
}
