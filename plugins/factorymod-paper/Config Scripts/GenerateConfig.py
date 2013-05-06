from ConfigObjects import Recipe, Enchant, ItemStack, Factory
from ParseConfig import ParseConfig

import pydot
coeffs={}

def main():
    print 'Running....'
    ItemStack.importMaterials()
    createConfigFile()

def createConfig():
    inputs={}
    outputs={}
    maintenanceInputs={}
    recipes={}
    factories={}
    inputDict={'Iron':'Iron Ingot','Diamond':'Diamond'}
    coeffs['t']={'Iron':2,'Diamond':8}
    coeffs['i']={'Helmet':5,'Chestplate':8,'Leggings':7,'Boots':4,'Sword':2,'Axe':3,'Pickaxe':3,'Spade':1,'Hoe':2}# Modifier for different branches of the tree, based on vanilla costs
    coeffs['b']={'Helmet':1,'Chestplate':1,'Leggings':1,'Boots':1,'Sword':1,'Axe':1,'Pickaxe':1,'Spade':1,'Hoe':1}
    for key,value in coeffs['b'].items():coeffs['b'][key]=value*10
    coeffs['e']={'Helmet':1,'Chestplate':1,'Leggings':1,'Boots':1,'Sword':1,'Axe':1,'Pickaxe':1,'Spade':1,'Hoe':1}
    for key,value in coeffs['e'].items():coeffs['e'][key]=value*0.7
    for tech in coeffs['t'].keys():
        for equipment in coeffs['i'].keys():
            inputs[tech+'_'+equipment]=[ItemStack(name=inputDict[tech],amount=coeffs['i'][equipment]*coeffs['b'][equipment]*coeffs['e'][equipment])]
            outputs[tech+'_'+equipment]=[ItemStack(name=tech+' '+equipment,amount=coeffs['b'][equipment])]
            recipes[tech+'_'+equipment]=Recipe(identifier=tech+'_'+equipment,name='Forge '+tech+' '+equipment+'.',inputs=inputs[tech+'_'+equipment],outputs=outputs[tech+'_'+equipment])
            inputs[tech+'_'+equipment+'_Smithy']=[input.modifyAmount(1) for input in inputs[tech+'_'+equipment]]
            maintenanceInputs[tech+'_'+equipment+'_Smithy']=[ItemStack(name=inputDict[tech])]
            factories[tech+'_'+equipment+'_Smithy']=Factory(identifier=tech+'_'+equipment+'_Smithy',name=tech+' '+equipment+' Smithy',inputs=inputs[tech+'_'+equipment+'_Smithy'],maintenanceInputs=maintenanceInputs[tech+'_'+equipment+'_Smithy'],outputRecipes=[recipes[tech+'_'+equipment]])
    
    oi={('Bread',1):[('Wheat',3)],('Baked Potato',1):[('Potato',1)],('Cooked Chicken',1):[('Raw Chicken',1)],('Cooked Beef',1):[('Raw Beef',1)],('Grilled Pork',1):[('Pork',1)],('Cooked Fish',1):[('Raw Fish',1)],('Cookie',8):[('Wheat',2),('Cocoa',1)]}
    bulk=64
    eff=0.7
    for output in oi.keys():
        identifier=output[0].replace(' ','_')
        inputs[identifier]=[]
        for input in oi[output]:
            inputs[identifier].append(ItemStack(name=input[0],amount=input[1]*bulk*eff))
        outputs[identifier]=[ItemStack(name=output[0],amount=output[1]*bulk)]
        recipes[identifier]=Recipe(identifier=identifier,name=output[0],inputs=inputs[identifier],outputs=outputs[identifier])
        inputs[identifier+'_Bakery']=[itemStack.modifyAmount(2) for itemStack in inputs[identifier]]
        factories[identifier+'_Bakery']=Factory(identifier=identifier+'_Bakery',name=output[0]+' Bakery',inputs=inputs[identifier+'_Bakery'],outputRecipes=[recipes[identifier]])
    
# 1 bottle
# 1 cooked meat (beef/pork/chicken/fish)
# 1 planted resource (wheat, netherwart, carrots, potatoes, cocoa)
# 1 automated resource (cactus, cane, melon slice, pumpkins)
# 1 mob drop (rotten flesh, spider eye, bone, ender pearl)
# 1 sheared resource (grass, vine, leaves)
# 1 mushroom (red/brown)
# 1 crafted food (bread, cookie, mushroom stew)
# 1 bonemeal resource (yellow/red flower, grass)
    inputs['Wood_Cauldron']=[ItemStack(name='Stick',amount=64)]
    inputs['Diamond_Cauldron']=[ItemStack(name='Diamond',amount=20)]
    inputs['Emerald_Cauldron']=[ItemStack(name='Emerald',amount=20)]
    factories['Wood_Cauldron']=Factory(identifier='Wood_Cauldron',name='Wood Cauldron',inputs=inputs['Wood_Cauldron'])
    factories['Diamond_Cauldron']=Factory(identifier='Diamond_Cauldron',name='Diamond Cauldron',inputs=inputs['Diamond_Cauldron'])
    factories['Emerald_Cauldron']=Factory(identifier='Emerald_Cauldron',name='Emerald Cauldron',inputs=inputs['Emerald_Cauldron'])
    cauldronInputs={}
    cauldronInputs['Wood']=[([('Glass Bottle',1),('Wheat',30)],1),([('Glass Bottle',1),('Carrot',30)],1),([('Glass Bottle',1),('Potato',30)],1),([('Glass Bottle',1),('Cocoa',30)],1)]
    cauldronInputs['Diamond']=[]
    cauldronInputs['Diamond'].append(([('Glass Bottle',1),('Red Rose',1),('Wheat',4),('Cactus',3),('Pork',1),('Red Mushroom',1)],1))
    cauldronInputs['Diamond'].append(([('Glass Bottle',1),('Yellow Flower',1),('Potato',4),('Sugar Cane',3),('Raw Chicken',1),('Brown Mushroom',1)],1))
    cauldronInputs['Diamond'].append(([('Glass Bottle',1),('Red Rose',1),('Carrot',4),('Melon Block',3),('Raw Beef',1),('Nether Warts',1)],1))
    cauldronInputs['Diamond'].append(([('Glass Bottle',1),('Yellow Flower',1),('Cocoa',4),('Pumpkin',3),('Raw Fish',1),('Brown Mushroom',1)],1))
    cauldronInputs['Emerald']=[]
    cauldronInputs['Emerald'].append(([('Glass Bottle',4),('Red Rose',1),('Wheat',4),('Cactus',3),('Pork',1),('Red Mushroom',1),('Carrot',4),('Melon Block',3),('Raw Beef',1),('Nether Warts',1)],4))
    cauldronInputs['Emerald'].append(([('Glass Bottle',4),('Yellow Flower',1),('Potato',4),('Sugar Cane',3),('Raw Chicken',1),('Red Mushroom',1),('Cocoa',4),('Pumpkin',3),('Raw Fish',1),('Brown Mushroom',1)],4))
    for cauldron in cauldronInputs.keys():
        i=0
        for recipeInput,bottles in cauldronInputs[cauldron]:
            identifier=cauldron+'_XP_Bottle_'+str(i)
            i+=1
            inputs[identifier]=[ItemStack(name=name,amount=amount) for name,amount in recipeInput]
            outputs[identifier]=[ItemStack(name='Exp Bottle',amount=bottles)]
            recipes[identifier]=Recipe(identifier=identifier,name='Brew XP Bottles  - '+str(i),inputs=inputs[identifier],outputs=outputs[identifier])
            factories[cauldron+'_Cauldron'].addRecipe(recipes[identifier])
   # eInputs=[[('Wheat',30)],[('Potato',30)],[('Wheat',10),('Potato',10)],[('Wheat',5),('Potato',5),('Cactus',1),('Cocoa',1)],[('Nether Warts',1),('Pumpkin',1),('Cactus',1),('Cocoa',1)]]
    # i=0
    # for input in eInputs:
        # identifier='XP_Bottle_'+str(i)
        # i+=1
        # inputs[identifier]=[ItemStack(name=name,amount=amount) for name,amount in input]
        # outputs[identifier]=[ItemStack(name='Exp Bottle',amount=5)]
        # recipes[identifier]=Recipe(identifier=identifier,name='Brew XP Bottles  - '+str(i),inputs=inputs[identifier],outputs=outputs[identifier])
        # factories['Cauldron'].addRecipe(recipes[identifier])
    return (factories,recipes)
        
def createConfigFile():
    config={}
    config['copy_defaults']='true'
    config['central_block']='WORKBENCH'
    config['save_cycle']='15'
    config['return_build_materials']='false'
    config['citadel_enabled']='true'
    config['factory_interaction_material']='STICK'
    config['destructible_factories']='false'
    config['disable_experience']='true'
    config['disrepair_length']='15'
    config['update_cycle']='20'
    config['maintenance_cycle']='6000'
    config['maintenance_rate']='0.00000165'
    
    config['factories'],config['recipes']=createConfig()

    config['disabled_recipes']=[]
    checkConflicts(config['factories'])
    print 'Fixing Conflicts...'
    fixConflicts(config['factories'])
    checkConflicts(config['factories'])
    ParseConfig.saveConfig(config)
    ParseConfig.prettyList(config)
    
def checkConflicts(factories):
    for factory in factories.values():
        for itemStack in factory.inputs:
            for otherFactory in factories.values():
                for otherItemStack in otherFactory.inputs:
                    if factory!=otherFactory:
                        if itemStack.equals(otherItemStack):
                            print 'Conflict  of '+factory.name+' and '+otherFactory.name
def fixConflicts(factories):
    for factory in factories.values():
        for itemStack in factory.inputs:
            for otherFactory in factories.values():
                for otherItemStack in otherFactory.inputs:
                    if factory!=otherFactory:
                        if itemStack.equals(otherItemStack):
                            itemStack.amount+=1

                            
if __name__ == '__main__':
    main()