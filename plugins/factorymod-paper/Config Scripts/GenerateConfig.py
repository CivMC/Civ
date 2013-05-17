from ConfigObjects import Recipe, Enchantment, ItemStack, Factory, CraftedRecipe
from ParseConfig import ParseConfig

coeffs={}
gMod=1
mMod=0.1

def main():
    print 'Running....'
    ItemStack.importMaterials()
    Enchantment.importEnchantments()
    createConfigFile()
    
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
    config['update_cycle']='20'
    config['repair_period']='28'
    config['disrepair_period']='14'
    config['factories'],config['recipes']=createFactorieAndRecipes()
    config['disabled_recipes']=[]
    config['enabled_recipes']=createCraftingRecipes()
    checkConflicts(config['factories'])
    print 'Fixing Conflicts...'
    fixConflicts(config['factories'])
    checkConflicts(config['factories'])
    ParseConfig.saveConfig(config)
    ParseConfig.prettyList(config)
    
def createFactorieAndRecipes():
    inputs={}
    outputs={}
    enchantments={}
    recipes={}
    factories={}
    
    #Smelting
    #Stone
    id='Smelt_Stone'
    inputs[id]=[ItemStack(name='Cobblestone',amount=640)]
    outputs[id]=[ItemStack(name='Stone',amount=640*1.333)]
    recipes[id]=Recipe(identifier=id,name='Smelt Stone',inputs=inputs[id],outputs=outputs[id],time=120)
    id='Stone_Smelter'
    inputs[id]=[ItemStack(name='Stone',amount=2048*gMod)]
    factories[id]=Factory(identifier=id,name='Stone Smelter',inputs=inputs[id],outputRecipes=[recipes['Smelt_Stone']])
    #Charcoal
    woods=['Oak Wood','Spruce Wood','Birch Wood','Jungle Wood']
    id='Charcoal_Smelter'
    inputs[id]=[ItemStack(name='Charcoal',amount=300*gMod)]
    factories[id]=Factory(identifier=id,name='Charcoal Burner',inputs=inputs[id])
    for wood in woods:
        id='Smelt_'+wood.replace(' ','_')
        inputs[id]=[ItemStack(name=wood,amount=256)]
        outputs[id]=[ItemStack(name='Charcoal',amount=256*1.333)]
        recipes[id]=Recipe(identifier=id,name='Burn '+wood,inputs=inputs[id],outputs=outputs[id],time=256/8*3/4)
        factories['Charcoal_Smelter'].addRecipe(recipes[id])
    #Smelter
    ores={'Coal Ore':('Coal',512,3,128),'Iron Ore':('Iron Ingot',384,1.75,128),'Gold Ore':('Gold Ingot',192,1.75,64),'Diamond Ore':('Diamond',96,3,16)}
    inputs['Smelter']=[ItemStack(name=values[0],amount=values[1]) for ore,values in ores.items()]
    factories['Smelter']=Factory(identifier='Smelter',name='Ore Smelter',inputs=inputs['Smelter'])
    for ore,values in ores.items():
        id='Smelt_'+ore.replace(' ','_')
        inputs[id]=[ItemStack(name=ore,amount=values[3])]
        outputs[id]=[ItemStack(name=values[0],amount=values[3]*values[2])]
        recipes[id]=Recipe(identifier=id,name='Smelt '+ore,inputs=inputs[id],outputs=outputs[id],time=values[3]/8*3/4)
        factories['Smelter'].addRecipe(recipes[id])
    
    #Equipment
    enchantmentData=[]
    enchantmentData.extend([('Unbreaking',[(3,1)]),('Silk Touch',[(1,0.1)]),('Efficiency',[(1,.3),(2,.2),(3,0.1),(4,0.05),(5,0.01)])])
    enchantmentData.extend([('Bane of the Anthropods',[(1,.4),(2,.3),(3,.2),(4,.1),(5,0.3)]),('Smite',[(1,.4),(2,.3),(3,.2),(4,.1),(5,0.05)]),('Looting',[(1,0.5),(2,0.4),(3,0.3)])])
    enchantmentData.extend([('Respiration',[(1,0.5),(2,0.4),(3,0.3),(4,0.4)]),('Blast Protection',[(1,0.5),(2,0.4),(3,0.3),(4,0.4)]),('Feather Falling',[(1,0.5),(2,0.4),(3,0.3),(4,0.4)]),('Fire Protection',[(1,0.5),(2,0.4),(3,0.3),(4,0.4)]),('Projectile Protection',[(1,0.5),(2,0.4),(3,0.3),(4,0.4)])])
    enchantmentsInputs=sum([[Enchantment(name=name,level=level,probability=prob) for level,prob in pairs] for name,pairs in enchantmentData],[])

    inputDict={'Iron':'Iron Ingot','Gold':'Gold Ingot','Diamond':'Diamond'}
    coeffs['i']={'Helmet':5,'Chestplate':8,'Leggings':7,'Boots':4,'Sword':2,'Axe':3,'Pickaxe':3,'Spade':1,'Hoe':2}# Modifier for different branches of the tree, based on vanilla costs
    coeffs['b']={'Helmet':1,'Chestplate':1,'Leggings':1,'Boots':1,'Sword':1,'Axe':1,'Pickaxe':1,'Spade':1,'Hoe':1}
    for key,value in coeffs['b'].items():coeffs['b'][key]=value*5
    coeffs['e']={'Helmet':3,'Chestplate':3,'Leggings':3,'Boots':3,'Sword':3,'Axe':6,'Pickaxe':3,'Spade':3,'Hoe':6}
    buildCosts={'Helmet':192,'Chestplate':320,'Leggings':256,'Boots':160,'Sword':80,'Axe':64,'Pickaxe':96,'Spade':48,'Hoe':32}
    for tech in inputDict.keys():
        for equipment in coeffs['i'].keys():
            enchantments[tech+'_'+equipment]=[]
            if tech=='Gold':
                enchantments[tech+'_'+equipment]=list(enchantmentsInputs)
            inputs[tech+'_'+equipment]=[ItemStack(name=inputDict[tech],amount=coeffs['i'][equipment]*coeffs['b'][equipment])]
            outputs[tech+'_'+equipment]=[ItemStack(name=tech+' '+equipment,amount=coeffs['b'][equipment]*coeffs['e'][equipment])]
            recipes[tech+'_'+equipment]=Recipe(identifier=tech+'_'+equipment,name='Forge '+tech+' '+equipment+'.',inputs=inputs[tech+'_'+equipment],outputs=outputs[tech+'_'+equipment],enchantments=enchantments[tech+'_'+equipment])
            inputs[tech+'_'+equipment+'_Smithy']=[ItemStack(name=inputDict[tech],amount=buildCosts[equipment])]
            factories[tech+'_'+equipment+'_Smithy']=Factory(identifier=tech+'_'+equipment+'_Smithy',name=tech+' '+equipment+' Smithy',inputs=inputs[tech+'_'+equipment+'_Smithy'],outputRecipes=[recipes[tech+'_'+equipment]])
    
    #Food output:([inputs],build cost,efficieny,bulk)
    #Butchers
    oi={('Cooked Chicken',1):([('Raw Chicken',1)],192,2,64),('Grilled Pork',1):([('Pork',1)],160,2,64),('Cooked Beef',1):([('Raw Beef',1)],64,2,64),('Cooked Fish',1):([('Raw Fish',1)],16,2,64)}
    id='Grill'
    inputs[id]=[ItemStack(name=key[0],amount=value[1]) for key,value in oi.items()]
    factories[id]=Factory(identifier=id,name='Bakery',inputs=inputs[id])
    for key,value in oi.items():
        id=key[0].replace(' ','_')
        inputs[id]=[ItemStack(name=name,amount=amount*value[3]) for name,amount in value[0]]
        outputs[id]=[ItemStack(name=key[0],amount=key[1]*value[2]*value[3])]
        recipes[id]=Recipe(identifier=id,name='Grill '+name,inputs=inputs[id],outputs=outputs[id],time=inputs[id][0].amount/8*3/4)
        factories['Grill'].addRecipe(recipes[id])
    #Bakery
    oi={('Bread',1):([('Wheat',3)],256,2,128),('Baked Potato',1):([('Potato',1)],512,2,192),('Cookie',8):([('Wheat',2),('Cocoa',1)],1024,2,128)}
    id='Bakery'
    inputs[id]=[ItemStack(name=key[0],amount=value[1]) for key,value in oi.items()]
    factories[id]=Factory(identifier=id,name='Bakery',inputs=inputs[id])
    for key,value in oi.items():
        id=key[0].replace(' ','_')
        inputs[id]=[ItemStack(name=name,amount=amount*value[3]) for name,amount in value[0]]
        outputs[id]=[ItemStack(name=key[0],amount=key[1]*value[2]*value[3])]
        recipes[id]=Recipe(identifier=id,name='Bake '+name,inputs=inputs[id],outputs=outputs[id],time=256/8*3/4)
        factories['Bakery'].addRecipe(recipes[id])
    
    #Wool
    inputColors=['White', 'Light Gray', 'Gray', 'Black', 'Brown', 'Pink']
    dyes={'White':'Bone Meal','Light Gray':'Light Gray Dye','Gray':'Gray Dye','Black':'Ink Sack','Red':'Rose Red','Orange':'Orange Dye','Yellow':'Dandelion Yellow','Lime':'Lime Dye','Green':'Cactus Green','Cyan':'Cyan Dye','Light Blue':'Light Blue Dye','Blue':'Lapis Lazuli','Purple':'Purple Dye','Magenta':'Magenta Dye','Pink':'Pink Dye','Brown':'Cocoa'}
    for inputColor in inputColors:
        factoryId=inputColor.replace(' ','_')+'_Wool_Processing'
        inputs[factoryId]=[ItemStack(name=dye,amount=20*gMod) for dye in dyes.values()]+[ItemStack(name=inputColor+' Wool',amount=20)]
        factories[factoryId]=Factory(identifier=factoryId,name=inputColor+' Wool Processing',inputs=inputs[factoryId])
        for outputColor,dye in dyes.items():
            if inputColor!=outputColor:
                id='Dye_'+inputColor.replace(' ','_')+'_Wool_'+outputColor.replace(' ','_')
                inputs[id]=[ItemStack(name=inputColor+' Wool',amount=64),ItemStack(name=dyes[outputColor],amount=4)]
                outputs[id]=[ItemStack(name=outputColor+' Wool',amount=64)]
                recipes[id]=Recipe(identifier=id,name='Dye '+inputColor+' Wool '+outputColor,inputs=inputs[id],outputs=outputs[id])
                factories[factoryId].addRecipe(recipes[id]) 
    
    #Enchanting
    inputs['Wood_Cauldron']=[ItemStack(name='Stick',amount=1024*gMod)]
    inputs['Iron_Cauldron']=[ItemStack(name='Iron Ingot',amount=200*gMod)]
    inputs['Diamond_Cauldron']=[ItemStack(name='Diamond',amount=50*gMod)]
    factories['Wood_Cauldron']=Factory(identifier='Wood_Cauldron',name='Wood Cauldron',inputs=inputs['Wood_Cauldron'])
    factories['Iron_Cauldron']=Factory(identifier='Iron_Cauldron',name='Iron Cauldron',inputs=inputs['Iron_Cauldron'])
    factories['Diamond_Cauldron']=Factory(identifier='Diamond_Cauldron',name='Diamond Cauldron',inputs=inputs['Diamond_Cauldron'])
    cauldronInputs={}
    cauldronInputs['Wood']=[]
    cauldronInputs['Wood'].append(([('Glass Bottle',14),('Wheat',1280)],14))
    cauldronInputs['Wood'].append(([('Glass Bottle',7),('Nether Warts',1280)],7))
    cauldronInputs['Wood'].append(([('Glass Bottle',24),('Baked Potato',1280)],24))
    cauldronInputs['Wood'].append(([('Glass Bottle',6),('Cookie',1280)],6))
    cauldronInputs['Iron']=[]
    cauldronInputs['Iron'].append(([('Glass Bottle',32),('Carrot',128),('Cocoa',128),('Pumpkin',64),('Cactus',384),('Bread',64),('Cooked Beef',128)],32))
    cauldronInputs['Iron'].append(([('Glass Bottle',32),('Nether Warts',128),('Melon',32),('Sugar Cane',192),('Cookie',256),('Baked Potato',64),('Grilled Pork',128)],32))
    cauldronInputs['Diamond']=[]
    cauldronInputs['Diamond'].append(([('Glass Bottle',64),('Carrot',96),('Melon',32),('Cactus',256),('Red Rose',8),('Rotten Flesh',64),('Red Mushroom',32),('Vine',48),('Bread',128),('Grilled Pork',128)],64))
    cauldronInputs['Diamond'].append(([('Glass Bottle',64),('Nether Warts',64),('Melon',32),('Sugar Cane',128),('Yellow Flower',16),('Spider Eye',64),('Brown Mushroom',64),('Vine',64),('Baked Potato',128),('Cooked Chicken',128)],64))
    cauldronInputs['Diamond'].append(([('Glass Bottle',64),('Wheat',512),('Cocoa',32),('Pumpkin',128),('Cactus',256),('Red Rose',16),('Spider Eye',64),('Grass',128),('Cooked Fish',16)],64))
    cauldronInputs['Diamond'].append(([('Glass Bottle',64),('Nether Warts',128),('Pumpkin',128),('Sugar Cane',192),('Yellow Flower',16),('Spider Eye',64),('Brown Mushroom',64),('Grass',128),('Cookie',512),('Cooked Beef',128)],64))
    for cauldron in cauldronInputs.keys():
        i=0
        for recipeInput,bottles in cauldronInputs[cauldron]:
            id=cauldron+'_XP_Bottle_'+str(i)
            i+=1
            inputs[id]=[ItemStack(name=name,amount=amount) for name,amount in recipeInput]
            outputs[id]=[ItemStack(name='Exp Bottle',amount=bottles)]
            recipes[id]=Recipe(identifier=id,name='Brew XP Bottles  - '+str(i),inputs=inputs[id],outputs=outputs[id])
            factories[cauldron+'_Cauldron'].addRecipe(recipes[id])
            
            #inputs[id+'_Bulk']=[itemStack.modifyAmount(64) for itemStack in recipes[id].inputs]
            #outputs[id+'_Bulk']=[itemStack.modifyAmount(64) for itemStack in recipes[id].outputs]
            #recipes[id+'_Bulk']=Recipe(identifier=id+'_Bulk',name='Brew XP Bottles  - '+str(i),inputs=inputs[id+'_Bulk'],outputs=outputs[id+'_Bulk'],time=128)
            #factories[cauldron+'_Cauldron'].addRecipe(recipes[id+'_Bulk'])

    
    #Add in repair
    for factory in factories.values():
        factory.repairMultiple=min([input.amount for input in [input.modifyAmount(mMod) for input in factory.inputs]])
        factory.repairInputs=[input.modifyAmount(1.0/factory.repairMultiple) for input in [input.modifyAmount(mMod) for input in factory.inputs]]
    return (factories,recipes)

def createCraftingRecipes():
        enabledRecipes=[]
        enabledRecipes.append(CraftedRecipe('XP to Emerald',inputs={'a':ItemStack('Exp Bottle',amount=9)},output=ItemStack('Emerald')))
        enabledRecipes.append(CraftedRecipe('Emerald to XP',inputs={'a':ItemStack('Emerald')},output=ItemStack('Exp Bottle',amount=9)))
        enabledRecipes.append(CraftedRecipe('Stone to Double Slab',inputs={'s':ItemStack('Stone')},shape=['sss','sss'],output=ItemStack('Double Stone Slab')))
        enabledRecipes.append(CraftedRecipe('Slab to Double Slab',inputs={'s':ItemStack('Stone Slab')},shape=['s','s'],output=ItemStack('Double Stone Slab')))
        return enabledRecipes
    
def checkConflicts(factories):
    for factory in factories.values():
        for itemStack in factory.inputs:
            for otherFactory in factories.values():
                for otherItemStack in otherFactory.inputs:
                    if factory!=otherFactory and 'Wool' not in factory.name and 'Smelter' not in factory.name+otherFactory.name:#Crappy fix here, but too much beer so I can't think 
                        if itemStack.equals(otherItemStack):
                            print 'Conflict  of '+factory.name+' and '+otherFactory.name
def fixConflicts(factories):
    for factory in factories.values():
        for itemStack in factory.inputs:
            for otherFactory in factories.values():
                for otherItemStack in otherFactory.inputs:
                    if factory!=otherFactory and 'Wool' not in factory.name and 'Smelter' not in factory.name+otherFactory.name:
                        if itemStack.equals(otherItemStack):
                            itemStack.amount+=1

                            
if __name__ == '__main__':
    main()