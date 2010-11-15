
includeTargets << grailsScript("Package")
includeTargets << grailsScript("_GrailsBootstrap")

USAGE = """
    constraintstrings [Overwrite]

where
	Overwrite = Should current translations be replaced. Default false. Useful to not overwrite any hand tuned translations.
"""

target(main: "Automatically extract the set of keys used to render i18n error messages for constraints applied to domain classes") {
	def overwrite = parseArgs()
	
	bootstrap()

	println "Acquiring translation keys for constraints applied to domain classes within this project"

	def f = new File("${basedir}/grails-app/i18n/messages-domain.properties")
	if(!f.exists())
		f.createNewFile()

	def props = new SortedProperties()
	def inf = new FileInputStream(f)
	props.load(inf)

	grailsApp.domainClasses?.each { dc ->
		println "Extracting constraints from: ${dc.getFullName()}"
		dc.constraints.each { c ->
			c.value.getAppliedConstraints().sort{it.name}.each {
				def key = "${dc.getFullName()}.${c.key}.${it.name}".toString()
				def val = "".toString()
				if((!props.containsKey(key) || overwrite) && (!it.name.equals('nullable') || !c.value.isNullable()))
				props.put(key, val)
			}
		}
	}

	FileOutputStream outf = new FileOutputStream(new File("${basedir}/grails-app/i18n/messages-domain.properties"))
	props.store(outf, "Domain class constraint keys by Grails i18n constraint extractor plugin - Authored by Bradley Beddoes (http://bradleybeddoes.com)".toString())
	
	println "Acquired ${props.size()} translation keys for constraints applied to domain classes within this project"

}

setDefaultTarget(main)

def parseArgs() {
	args = args ? args.split('\n') : []
	switch (args.size()) {
		case 0:
			return false
			break
		case 1:
			return args[1] == "true"
			break
		default:
			usage()
			break
	}
}

private void usage() {
	println "Usage:\n${USAGE}"
	System.exit(1)
}

public class SortedProperties extends Properties {
  @Override
  public synchronized Enumeration keys() {
     def keysEnum = super.keys()
     def keyList = new Vector()
     while(keysEnum.hasMoreElements()){
		keyList.add(keysEnum.nextElement())
     }
     Collections.sort(keyList);
     keyList.elements();
  }
}
