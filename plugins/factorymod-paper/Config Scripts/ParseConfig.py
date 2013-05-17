from ConfigObjects import Recipe, Enchantment, ItemStack, Factory, defaults
import ConfigObjects
import pydot

class ParseConfig:
    @staticmethod
    def prettyList(config,filename='prettylist.txt'):
        myfile=open(filename,'w')
        myfile.write('\n\n##Factory List')
        sortedFactoryKeys=config['factories'].keys()
        sortedFactoryKeys.sort()
        types=[('Enchanting',['Cauldron']),('Smelting',['Smelter']),('Food',['Bakery','Grill']),('Equipment',['Smithy']),('Wool',['Wool'])]
        for type,names in types:
            myfile.write('\n\n-  ['+type+'](https://github.com/gmlaxfanatic/FactoryMod/wiki#'+type.lower()+')')        
        for type,names in types:
            myfile.write('\n\n###'+type)
            for key in sortedFactoryKeys:
                factory=config['factories'][key]
                if sum([(name in factory.identifier) for name in names])>0:
                    myfile.write('\n\n**'+factory.name+'**')
                    myfile.write(' - ')
                    for input in factory.inputs:
                        myfile.write(str(input.amount)+' '+input.name)
                        if(factory.inputs.index(input)!=len(factory.inputs)-1):
                                myfile.write(', ')
                    for recipe in factory.outputRecipes:
                        myfile.write('\n\n\t')
                        for output in recipe.outputs:
                            myfile.write(str(output.amount)+' '+output.name)
                        myfile.write(' for ')
                        for input in recipe.inputs:
                            myfile.write(str(input.amount)+' '+input.name)
                            if(recipe.inputs.index(input)!=len(recipe.inputs)-1):
                                myfile.write(', ')
                        myfile.write(' using '+str(recipe.time/factory.fuelTime)+' '+factory.fuel.name)

    @staticmethod
    def saveConfig(config,filename='config.yml'):
        from shutil import copyfile
        copyfile('template.yml',filename)
        myfile=open(filename,'a')
        myfile.write('copy_defaults: '+config['copy_defaults'])
        myfile.write('\ngeneral:')
        myfile.write('\n  central_block: '+config['central_block'])
        myfile.write('\n  save_cycle: '+config['save_cycle'])
        myfile.write('\n  return_build_materials: '+config['return_build_materials'])
        myfile.write('\n  citadel_enabled: '+config['citadel_enabled'])
        myfile.write('\n  factory_interaction_material: '+config['factory_interaction_material'])
        myfile.write('\n  disable_experience: '+config['disable_experience'])
        myfile.write('\nproduction_general:')
        myfile.write('\n  update_cycle: '+config['update_cycle'])
        myfile.write('\n  repair_period: '+config['repair_period'])
        myfile.write('\n  disrepair_period: '+config['disrepair_period'])
        myfile.write('\ncrafting:')
        myfile.write('\n  disable:')
        for disabled_recipe in config['disabled_recipes']:
            myfile.write('\n    - '+disabled_recipe)
        myfile.write('\n  enable:')
        for enabledRecipe in config['enabled_recipes']:
            myfile.write(enabledRecipe.cOutput())
        myfile.write('\nproduction_factories:')
        sortedFactoryKeys=config['factories'].keys()
        sortedFactoryKeys.sort()
        for key in sortedFactoryKeys:
            myfile.write(config['factories'][key].cOutput())
        myfile.write('\nproduction_recipes:')
        for recipe in config['recipes'].values():
            myfile.write(recipe.cOutput())
        myfile.close()